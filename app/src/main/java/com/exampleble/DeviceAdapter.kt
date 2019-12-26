package com.exampleble

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import java.util.ArrayList


class DeviceAdapter(private val context: Context) : BaseAdapter() {

    private var bleDeviceList: MutableList<BleDevice>
    private var mListener: OnDeviceClickListener? = null


    init {
        bleDeviceList = ArrayList()
    }

    fun addDevice(bleDevice: BleDevice) {
        removeDevice(bleDevice)
        bleDeviceList.add(bleDevice)
    }

    fun removeDevice(bleDevice: BleDevice) {
        try{
            for (i in bleDeviceList.indices) {
                val device = bleDeviceList[i]
                if (bleDevice.key == device.key) {
                    bleDeviceList.removeAt(i)
                }
            }
        }catch (e: Exception){

        }

    }

    fun clearConnectedDevice() {
        for (i in bleDeviceList.indices) {
            val device = bleDeviceList[i]
            if (BleManager.getInstance().isConnected(device)) {
                bleDeviceList.removeAt(i)
            }
        }
    }

    fun clearScanDevice() {

        bleDeviceList = ArrayList()

        for (i in bleDeviceList.indices) {
            val device = bleDeviceList[i]
            if (!BleManager.getInstance().isConnected(device)) {
                bleDeviceList.removeAt(i)
            }
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView != null) {
            holder = convertView.tag as ViewHolder
        } else {
            convertView = View.inflate(context, R.layout.adapter_device, null)
            holder = ViewHolder()
            convertView!!.tag = holder
            holder.imgblue = convertView.findViewById(R.id.img_blue) as ImageView
            holder.txtname = convertView.findViewById(R.id.txt_name) as TextView
            holder.txtmac = convertView.findViewById(R.id.txt_mac) as TextView
            holder.txtrssi = convertView.findViewById(R.id.txt_rssi) as TextView
            holder.layoutidle = convertView.findViewById(R.id.layout_idle) as LinearLayout
            holder.layoutconnected =
                convertView.findViewById(R.id.layout_connected) as LinearLayout
            holder.btndisconnect = convertView.findViewById(R.id.btn_disconnect) as Button
            holder.btnconnect = convertView.findViewById(R.id.btn_connect) as Button
            holder.btndetail = convertView.findViewById(R.id.btn_detail) as Button
        }

        val bleDevice = getItem(position)
        if (bleDevice != null) {
            val isConnected = BleManager.getInstance().isConnected(bleDevice)
            val name = bleDevice.name
            val mac = bleDevice.mac
            val rssi = bleDevice.rssi
            holder.txtname!!.text = name
            holder.txtmac!!.text = mac
            holder.txtrssi!!.text = rssi.toString()
            if (isConnected) {
                holder.txtname!!.setTextColor(-0xe2164a)
                holder.txtmac!!.setTextColor(-0xe2164a)
                holder.layoutidle!!.visibility = View.GONE
                holder.layoutconnected!!.visibility = View.VISIBLE
            } else {
                holder.txtname!!.setTextColor(-0x1000000)
                holder.txtmac!!.setTextColor(-0x1000000)
                holder.layoutidle!!.visibility = View.VISIBLE
                holder.layoutconnected!!.visibility = View.GONE
            }
        }

        holder.btnconnect!!.setOnClickListener {
            if (mListener != null) {
                mListener!!.onConnect(bleDevice)
            }
        }

        holder.btndisconnect!!.setOnClickListener {
            if (mListener != null) {
                mListener!!.onDisConnect(bleDevice)
            }
        }

        holder.btndetail!!.setOnClickListener {
            if (mListener != null) {
                mListener!!.onDetail(bleDevice)
            }
        }

        return convertView
    }

    internal inner class ViewHolder {
        var imgblue: ImageView? = null
        var txtname: TextView? = null
        var txtmac: TextView? = null
        var txtrssi: TextView? = null
        var layoutidle: LinearLayout? = null
        var layoutconnected: LinearLayout? = null
        var btndisconnect: Button? = null
        var btnconnect: Button? = null
        var btndetail: Button? = null
    }

    interface OnDeviceClickListener {


        fun onConnect(bleDevice: BleDevice?)

        fun onDisConnect(bleDevice: BleDevice?)

        fun onDetail(bleDevice: BleDevice?)
    }

    fun setOnDeviceClickListener(listener: OnDeviceClickListener) {
        this.mListener = listener
    }

}