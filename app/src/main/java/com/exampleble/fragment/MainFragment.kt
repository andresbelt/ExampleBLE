package com.exampleble.fragment

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
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

class MainFragment : Fragment(), DeviceAdapter.OnDeviceClickListener {

    private var deviceAdapter: DeviceAdapter? = null
    private var layoutSetting: LinearLayout? = null
    private var buttons: LinearLayout? = null
    private var buttonScan: Button? = null
    private var buttonWrite: Button? = null
    private var buttonNotify: Button? = null
    private var imageLoading: ImageView? = null

    private var textTitle: TextView? = null
    private var text: TextView? = null

    private var operatingAnimation: Animation? = null
    private var progressDialog: ProgressDialog? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onConnect(bleDevice: BleDevice?) {
        if (!BleManager.getInstance().isConnected(bleDevice)) {
            BleManager.getInstance().cancelScan()

            connect(bleDevice)
        }
    }

    override fun onDisConnect(bleDevice: BleDevice?) {
        if (BleManager.getInstance().isConnected(bleDevice)) {
            buttons?.visibility = View.INVISIBLE
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

        BleManager.getInstance().init(activity?.application)
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
        val view: View = inflater.inflate(R.layout.fragment_main, null)

        buttonScan = view.findViewById(R.id.button_scan) as Button
        buttonScan?.text = getString(R.string.start_scan)
        buttonScan?.setOnClickListener {
            if (buttonScan?.text == getString(R.string.start_scan)) {
                checkPermissions()
            } else if (buttonScan?.text == getString(R.string.stop_scan)) {
                BleManager.getInstance().cancelScan()
            }

            val bleDevice = (activity as MainActivity).bleDevice

            if (BleManager.getInstance().isConnected(bleDevice)) {
                onButtonPressed(bleDevice)
            }
        }

        textTitle = view.findViewById(R.id.text_title)

        text = view.findViewById(R.id.txt)

        layoutSetting?.visibility = View.GONE

        buttons = view.findViewById(R.id.buttons)
        buttonWrite = view.findViewById(R.id.button_write)
        buttonNotify = view.findViewById(R.id.button_notify)

        imageLoading = view.findViewById(R.id.image_loading)
        operatingAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate)
        operatingAnimation?.interpolator = LinearInterpolator()
        progressDialog = ProgressDialog(context)

        deviceAdapter = DeviceAdapter(context)

        deviceAdapter?.setOnDeviceClickListener(this)

        val listViewDevice = view.findViewById<ListView>(R.id.list_device)
        listViewDevice.adapter = deviceAdapter

        return view
    }

    fun onButtonPressed(BleDevice: BleDevice?) {
        listener?.onFragmentInteraction(BleDevice)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
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
        if (permissionDeniedList.isNotEmpty()) {
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
                        .setNegativeButton(R.string.cancel
                        ) { _, _ -> activity?.finish() }
                        .setPositiveButton(R.string.setting
                        ) { _, _ ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    startScan()
                }
        }
    }

    private fun showConnectedDevice() {
        val deviceList = BleManager.getInstance().allConnectedDevice
        deviceAdapter?.clearConnectedDevice()

        deviceList?.forEach { device ->
            deviceAdapter?.addDevice(device)
        }
        deviceAdapter?.notifyDataSetChanged()
    }

    private fun startScan() {
        BleManager.getInstance().scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                deviceAdapter?.clearScanDevice()
                deviceAdapter?.notifyDataSetChanged()
                imageLoading?.startAnimation(operatingAnimation)
                imageLoading?.visibility = View.VISIBLE
                buttonScan?.text = getString(R.string.stop_scan)
            }

            override fun onLeScan(bleDevice: BleDevice?) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice) {
                deviceAdapter?.addDevice(bleDevice)
                deviceAdapter?.notifyDataSetChanged()
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                imageLoading?.clearAnimation()
                imageLoading?.visibility = View.INVISIBLE
                buttonScan?.text = getString(R.string.start_scan)
            }
        })
    }

    private fun connect(bleDevice: BleDevice?) {
        BleManager.getInstance().connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                progressDialog?.show()
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException) {
                imageLoading?.clearAnimation()
                imageLoading?.visibility = View.INVISIBLE
                buttonScan?.text = getString(R.string.start_scan)
                progressDialog?.dismiss()
                Toast.makeText(
                    context,
                    getString(R.string.connect_fail),
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
                progressDialog?.dismiss()
                deviceAdapter?.addDevice(bleDevice)
                deviceAdapter?.notifyDataSetChanged()
                onButtonPressed(bleDevice)

                deviceAdapter?.clearScanDevice()
                deviceAdapter?.notifyDataSetChanged()
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                bleDevice: BleDevice,
                gatt: BluetoothGatt,
                status: Int
            ) {
                progressDialog?.dismiss()
                buttons?.visibility = View.INVISIBLE

                deviceAdapter?.removeDevice(bleDevice)
                deviceAdapter?.notifyDataSetChanged()

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
                    ObserverManager.instance.notifyObserver(bleDevice)
                }
            }
        })
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(bleDevice: BleDevice?)
    }

    companion object {

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        private const val REQUEST_CODE_OPEN_GPS = 1
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2

        @JvmStatic
        fun newInstance(param1: String, param2: String) = MainFragment()
            .apply {
                arguments = Bundle()
                    .apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
            }
    }
}
