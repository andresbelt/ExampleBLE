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

class ServiceListFragment: Fragment() {

    private var textName: TextView? = null
    private var textMac: TextView? = null
    private var resultAdapter: ResultAdapter? = null

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
        textName = v.findViewById(R.id.txt_name)
        textMac = v.findViewById(R.id.txt_mac)

        resultAdapter = ResultAdapter(context)
        val listViewDevice = v.findViewById<ListView>(R.id.list_service)
        listViewDevice.adapter = resultAdapter
        listViewDevice.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val service = resultAdapter?.getItem(position)
                (activity as MainActivity).bluetoothGattService = service
                (activity as MainActivity).changePage(2)
            }
    }

    private fun showData() {
        val bleDevice = (activity as MainActivity).bleDevice

        val name = bleDevice?.name
        val mac = bleDevice?.mac
        val gatt = BleManager.getInstance().getBluetoothGatt(bleDevice)

        textName?.text = getString(R.string.name) + name
        textMac?.text = getString(R.string.mac) + mac

        resultAdapter?.clear()
        gatt.services.forEach { service ->
            resultAdapter?.addResult(service)
        }
        resultAdapter?.notifyDataSetChanged()
    }

    private inner class ResultAdapter internal constructor(private val context: Context?) : BaseAdapter() {

        private val bluetoothGattServices = mutableListOf<BluetoothGattService>()

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

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var view = convertView
            val holder: ViewHolder
            if (view != null) {
                holder = view.tag as ViewHolder
            } else {
                view = View.inflate(context,
                    R.layout.adapter_service, null)
                holder = ViewHolder()
                view?.tag = holder
                holder.textTitle = view.findViewById(R.id.text_title)
                holder.textUuid = view.findViewById(R.id.text_uuid)
                holder.textType = view.findViewById(R.id.text_type)
            }

            val service = bluetoothGattServices[position]
            val uuid = service.uuid.toString()

            holder.textTitle?.text = activity!!.getString(R.string.service) + "(" + position + ")"
            holder.textUuid?.text = uuid
            holder.textType?.text = activity!!.getString(R.string.type)
            return view
        }

        internal inner class ViewHolder {
            var textTitle: TextView? = null
            var textUuid: TextView? = null
            var textType: TextView? = null
        }
    }
}