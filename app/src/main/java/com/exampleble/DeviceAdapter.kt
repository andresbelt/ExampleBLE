package com.exampleble

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import java.util.*


class DeviceAdapter(private val context: Context?) : BaseAdapter() {

    private var bleDeviceList = mutableListOf<BleDevice>()
    private var listener: OnDeviceClickListener? = null

    fun addDevice(bleDevice: BleDevice) {
        removeDevice(bleDevice)
        bleDeviceList.add(bleDevice)
    }

    fun removeDevice(bleDevice: BleDevice) {
        try {
            bleDeviceList.forEachIndexed { index, device ->
                if (bleDevice.key == device.key) {
                    bleDeviceList.removeAt(index)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearConnectedDevice() {
        try {
            bleDeviceList.forEachIndexed { index, device ->
                if (BleManager.getInstance().isConnected(device)) {
                    bleDeviceList.removeAt(index)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearScanDevice() {
        bleDeviceList = ArrayList()

        try {
            bleDeviceList.forEachIndexed { index, device ->
                if (!BleManager.getInstance().isConnected(device)) {
                    bleDeviceList.removeAt(index)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clear() {
        clearConnectedDevice()
        clearScanDevice()
    }

    override fun getCount(): Int {
        return bleDeviceList.size
    }

    override fun getItem(position: Int): BleDevice? {
        return if (position > bleDeviceList.size) null else bleDeviceList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val holder: ViewHolder
        if (view != null) {
            holder = view.tag as ViewHolder
        } else {
            view = View.inflate(context, R.layout.adapter_device, null)
            with(ViewHolder()) {
                holder = this
                view?.tag = this

                imageBlue = view.findViewById(R.id.img_blue)
                textName = view.findViewById(R.id.txt_name)
                textMac = view.findViewById(R.id.txt_mac)
                textRssi = view.findViewById(R.id.txt_rssi)
                layoutTitle = view.findViewById(R.id.layout_idle)
                layoutConnected = view.findViewById(R.id.layout_connected)
                buttonDisconnect = view.findViewById(R.id.btn_disconnect)
                buttonConnect = view.findViewById(R.id.btn_connect)
                buttonDetail = view.findViewById(R.id.btn_detail)
            }
        }

        val bleDevice = getItem(position)
        if (bleDevice != null) {
            val isConnected = BleManager.getInstance().isConnected(bleDevice)
            val name = bleDevice.name
            val mac = bleDevice.mac
            val rssi = bleDevice.rssi

            with(holder) {
                textName?.text = name
                textMac?.text = mac
                textRssi?.text = rssi.toString()
                if (isConnected) {
                    textName?.setTextColor(-0xe2164a) // TODO: move to colors.xml
                    textMac?.setTextColor(-0xe2164a)
                    layoutTitle?.visibility = View.GONE
                    layoutConnected?.visibility = View.VISIBLE
                } else {
                    textName?.setTextColor(-0x1000000)
                    textMac?.setTextColor(-0x1000000)
                    layoutTitle?.visibility = View.VISIBLE
                    layoutConnected?.visibility = View.GONE
                }
            }
        }

        holder.buttonConnect?.setOnClickListener {
            listener?.onConnect(bleDevice)
        }

        holder.buttonDisconnect?.setOnClickListener {
            listener?.onDisConnect(bleDevice)
        }

        holder.buttonDetail?.setOnClickListener {
            listener?.onDetail(bleDevice)
        }

        return view
    }

    internal inner class ViewHolder {
        var imageBlue: ImageView? = null
        var textName: TextView? = null
        var textMac: TextView? = null
        var textRssi: TextView? = null
        var layoutTitle: LinearLayout? = null
        var layoutConnected: LinearLayout? = null
        var buttonDisconnect: Button? = null
        var buttonConnect: Button? = null
        var buttonDetail: Button? = null
    }

    interface OnDeviceClickListener {

        fun onConnect(bleDevice: BleDevice?)

        fun onDisConnect(bleDevice: BleDevice?)

        fun onDetail(bleDevice: BleDevice?)
    }

    fun setOnDeviceClickListener(listener: OnDeviceClickListener) {
        this.listener = listener
    }
}