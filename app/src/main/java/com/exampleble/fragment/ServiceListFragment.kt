package com.exampleble.fragment

import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.clj.fastble.BleManager
import com.exampleble.MainActivity
import com.exampleble.R
import java.util.ArrayList

class ServiceListFragment: Fragment() {

    private var txt_name: TextView? = null
    private var txt_mac:TextView? = null
    private var mResultAdapter: ResultAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_service_list, null)
        initView(v)
        showData()
        return v
    }


    private fun initView(v: View) {
        txt_name = v.findViewById(R.id.txt_name) as TextView
        txt_mac = v.findViewById(R.id.txt_mac) as TextView

        mResultAdapter = ResultAdapter(context!!)
        val listView_device = v.findViewById(R.id.list_service) as ListView
        listView_device.adapter = mResultAdapter
        listView_device.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val service = mResultAdapter!!.getItem(position)
                (activity as MainActivity).setBluetoothGattService(service!!)
                (activity as MainActivity).changePage(2)
            }


    }

    private fun showData() {
        val bleDevice = (activity as MainActivity).getBleDevice()

        val name = bleDevice!!.name
        val mac = bleDevice!!.mac
        val gatt = BleManager.getInstance().getBluetoothGatt(bleDevice)

        txt_name!!.text = activity!!.getString(R.string.name) + name
        txt_mac!!.setText(activity!!.getString(R.string.mac) + mac)

        mResultAdapter!!.clear()
        for (service in gatt.services) {
            mResultAdapter!!.addResult(service)
        }
        mResultAdapter!!.notifyDataSetChanged()
    }

    private inner class ResultAdapter internal constructor(private val context: Context) :
        BaseAdapter() {
        private val bluetoothGattServices: MutableList<BluetoothGattService>

        init {
            bluetoothGattServices = ArrayList()
        }

        internal fun addResult(service: BluetoothGattService) {
            bluetoothGattServices.add(service)
        }

        internal fun clear() {
            bluetoothGattServices.clear()
        }

        override fun getCount(): Int {
            return bluetoothGattServices.size
        }

        override fun getItem(position: Int): BluetoothGattService? {
            return if (position > bluetoothGattServices.size) null else bluetoothGattServices[position]
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
                convertView = View.inflate(context,
                    R.layout.adapter_service, null)
                holder = ViewHolder()
                convertView!!.tag = holder
                holder.txt_title = convertView.findViewById(R.id.txt_title) as TextView
                holder.txt_uuid = convertView.findViewById(R.id.txt_uuid) as TextView
                holder.txt_type = convertView.findViewById(R.id.txt_type) as TextView
            }

            val service = bluetoothGattServices[position]
            val uuid = service.uuid.toString()

            holder.txt_title!!.text = activity!!.getString(R.string.service) + "(" + position + ")"
            holder.txt_uuid!!.text = uuid
            holder.txt_type!!.text = activity!!.getString(R.string.type)
            return convertView
        }

        internal inner class ViewHolder {
            var txt_title: TextView? = null
            var txt_uuid: TextView? = null
            var txt_type: TextView? = null
        }
    }
}