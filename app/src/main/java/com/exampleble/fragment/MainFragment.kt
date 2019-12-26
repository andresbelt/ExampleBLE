package com.desarollobluetooth.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.exampleble.DeviceAdapter
import com.exampleble.MainActivity
import com.exampleble.R
import com.exampleble.observers.ObserverManager
import java.util.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val REQUEST_CODE_OPEN_GPS = 1
private const val REQUEST_CODE_PERMISSION_LOCATION = 2
private var mDeviceAdapter: DeviceAdapter? = null
private var layout_setting: LinearLayout? = null
private var btns_: LinearLayout? = null
private var btn_scan: Button? = null
private var btn_write: Button? = null
private var btn_notify: Button? = null
private var img_loading: ImageView? = null

private var txt_title: TextView? = null
private var txt: TextView? = null

private var operatingAnim: Animation? = null
private var progressDialog: ProgressDialog? = null
private var listener: MainFragment.OnFragmentInteractionListener? = null


class MainFragment : Fragment(), DeviceAdapter.OnDeviceClickListener {

    override fun onConnect(bleDevice: BleDevice?) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().cancelScan()

            connect(bleDevice)
        }
    }

    override fun onDisConnect(bleDevice: BleDevice?) {
        if (BleManager.getInstance().isConnected(bleDevice)) {
            btns_?.visibility = View.INVISIBLE
            BleManager.getInstance().disconnect(bleDevice)
        }
    }

    override fun onDetail(bleDevice: BleDevice?) {
        if (BleManager.getInstance().isConnected(bleDevice)) {

            onButtonPressed(bleDevice)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        BleManager.getInstance().init(activity!!.application)
        BleManager.getInstance()
            .enableLog(true)
            .setReConnectCount(1, 5000)
            .setConnectOverTime(20000).operateTimeout = 5000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(
            R.layout.fragment_main,
            null
        )

        btn_scan = view.findViewById(R.id.btn_scan) as Button
        btn_scan?.text = getString(R.string.start_scan)
        btn_scan?.setOnClickListener {

            if (btn_scan?.text == getString(R.string.start_scan)) {
                checkPermissions()
            } else if (btn_scan?.text == getString(R.string.stop_scan)) {
                BleManager.getInstance().cancelScan()
            }

            val bleDevice = (activity as MainActivity).getBleDevice()

            if (BleManager.getInstance().isConnected(bleDevice)) {
                onButtonPressed(bleDevice)
            }

        }

        txt_title = view.findViewById(R.id.txt_title) as TextView

        txt = view.findViewById(R.id.txt) as TextView


        layout_setting?.visibility = View.GONE

        btns_ = view.findViewById(R.id.btns_) as LinearLayout
        btn_write = view.findViewById(R.id.btn_write) as Button
        btn_notify = view.findViewById(R.id.btn_notify) as Button

        img_loading = view.findViewById(R.id.img_loading) as ImageView
        operatingAnim = AnimationUtils.loadAnimation(context, R.anim.rotate)
        operatingAnim?.interpolator = LinearInterpolator()
        progressDialog = ProgressDialog(context)

        mDeviceAdapter = DeviceAdapter(context!!)

        mDeviceAdapter?.setOnDeviceClickListener(this)


        btn_notify?.setOnClickListener {
          //  onClickNotify()
        }

        btn_write?.setOnClickListener {
          //  onClickWrite()
        }


        val listViewDevice = view.findViewById(R.id.list_device) as ListView
        listViewDevice.adapter = mDeviceAdapter


        return view
    }




    fun onButtonPressed(BleDvice: BleDevice?) {
        listener?.onFragmentInteraction(BleDvice)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener") as Throwable
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(activity, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show()
            return
        }

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionDeniedList = ArrayList<String>()
        for (permission in permissions) {
            val permissionCheck = context?.let { ContextCompat.checkSelfPermission(it, permission) }
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    deniedPermissions,
                    REQUEST_CODE_PERMISSION_LOCATION
                )
            }
        }
    }

    private fun onPermissionGranted(permission: String) {
        when (permission) {
            Manifest.permission.ACCESS_FINE_LOCATION ->
                if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.M && !checkGPSIsOpen()
                ) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.notifyTitle)
                        .setMessage(R.string.gpsNotifyMsg)
                        .setNegativeButton(R.string.cancel,
                            DialogInterface.OnClickListener { dialog, which -> activity?.finish() })
                        .setPositiveButton(R.string.setting,
                            DialogInterface.OnClickListener { dialog, which ->
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                            })

                        .setCancelable(false)
                        .show()
                } else {
                    startScan()
                }
        }
    }

    private fun showConnectedDevice() {
        val deviceList = BleManager.getInstance().allConnectedDevice
        mDeviceAdapter?.clearConnectedDevice()
        for (bleDevice in deviceList) {
            mDeviceAdapter?.addDevice(bleDevice)
        }
        mDeviceAdapter?.notifyDataSetChanged()
    }


    private fun startScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                mDeviceAdapter?.clearScanDevice()
                mDeviceAdapter?.notifyDataSetChanged()
                img_loading?.startAnimation(operatingAnim)
                img_loading?.visibility = View.VISIBLE
                btn_scan?.text = getString(R.string.stop_scan)
            }

            override fun onLeScan(bleDevice: BleDevice?) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice) {
                mDeviceAdapter?.addDevice(bleDevice)
                mDeviceAdapter?.notifyDataSetChanged()
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                img_loading?.clearAnimation()
                img_loading?.setVisibility(View.INVISIBLE)
                btn_scan?.setText(getString(R.string.start_scan))
            }
        })
    }

    private fun connect(bleDevice: BleDevice?) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                progressDialog?.show()
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                img_loading?.clearAnimation()
                img_loading?.setVisibility(View.INVISIBLE)
                btn_scan?.setText(getString(R.string.start_scan))
                progressDialog?.dismiss()
                Toast.makeText(
                    context,
                    getString(R.string.connect_fail),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                progressDialog?.dismiss()
                mDeviceAdapter?.addDevice(bleDevice)
                mDeviceAdapter?.notifyDataSetChanged()
                onButtonPressed(bleDevice)

                mDeviceAdapter?.clearScanDevice()
                mDeviceAdapter?.notifyDataSetChanged()


            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                progressDialog?.dismiss()
                btns_?.visibility = View.INVISIBLE

                mDeviceAdapter?.removeDevice(bleDevice)
                mDeviceAdapter?.notifyDataSetChanged()

                if (isActiveDisConnected) {
                    Toast.makeText(
                        context,
                        getString(R.string.active_disconnected),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.disconnected),
                        Toast.LENGTH_LONG
                    ).show()
                    ObserverManager.getInstance().notifyObserver(bleDevice)
                }

            }
        })
    }


    private fun checkGPSIsOpen(): Boolean {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                ?: return false
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
    }


    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(bledevice: BleDevice?)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
