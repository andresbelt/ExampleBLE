package com.exampleble.fragment

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.exampleble.MainActivity
import com.exampleble.R
import java.util.*

class CharacteristicListFragment: Fragment() {

    private var resultAdapter: ResultAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_characteric_list, null)
        initView(v)
        return v
    }

    private fun initView(v: View) {
        resultAdapter = ResultAdapter(context)
        val listViewDevice = v.findViewById<ListView>(R.id.list_service)
        listViewDevice.adapter = resultAdapter
        listViewDevice.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val characteristic = resultAdapter?.getItem(position)
            val propList = ArrayList<Int>()
            val propNameList = ArrayList<String>()
            val charaProp = characteristic?.properties ?: 0
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_READ)
                propNameList.add("Read")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_WRITE)
                propNameList.add("Write")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_WRITE_NO_RESPONSE)
                propNameList.add("Write No Response")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_NOTIFY)
                propNameList.add("Notify")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                propList.add(CharacteristicOperationFragment.PROPERTY_INDICATE)
                propNameList.add("Indicate")
            }

            (activity as MainActivity).let {
                if (propList.size > 1) {
                    AlertDialog.Builder(it)
                        .setTitle(activity!!.getString(R.string.select_operation_type))
                        .setItems(propNameList.toTypedArray()
                        ) { _, which ->
                            it.characteristic = characteristic
                            it.charaProp = propList[which]
                            it.changePage(3)
                        }
                        .show()
                }
                if (propList.size > 0) {
                    it.characteristic = characteristic
                    it.charaProp = propList[0]
                    it.changePage(3)
                }
            }
        }
    }

    fun showData() {
        val service = (activity as MainActivity).bluetoothGattService
        resultAdapter?.clear()
        service?.characteristics?.forEach { characteristic ->
            resultAdapter?.addResult(characteristic)
        }
        resultAdapter?.notifyDataSetChanged()
    }

    private inner class ResultAdapter internal constructor(private val context: Context?) : BaseAdapter() {

        private val characteristicList = mutableListOf<BluetoothGattCharacteristic>()

        internal fun addResult(characteristic: BluetoothGattCharacteristic) {
            characteristicList.add(characteristic)
        }

        internal fun clear() {
            characteristicList.clear()
        }

        override fun getCount(): Int {
            return characteristicList.size
        }

        override fun getItem(position: Int): BluetoothGattCharacteristic? {
            return if (position > characteristicList.size) null else characteristicList[position]
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
                view = View.inflate(context, R.layout.adapter_service, null)
                holder = ViewHolder()
                view?.tag = holder
                holder.textTitle = view.findViewById(R.id.text_title)
                holder.textUuid = view.findViewById(R.id.text_uuid)
                holder.textType = view.findViewById(R.id.text_type)
                holder.imageNext = view.findViewById(R.id.image_next)
            }

            val characteristic = characteristicList[position]
            val uuid = characteristic.uuid.toString()

            holder.textTitle?.text = (getString(R.string.characteristic) + "（" + position + ")")
            holder.textUuid?.text = uuid

            val property = StringBuilder()
            val charaProp = characteristic.properties
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                property.append("Read")
                property.append(" , ")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {
                property.append("Write")
                property.append(" , ")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {
                property.append("Write No Response")
                property.append(" , ")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                property.append("Notify")
                property.append(" , ")
            }
            if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                property.append("Indicate")
                property.append(" , ")
            }
            if (property.length > 1) {
                property.delete(property.length - 2, property.length - 1)
            }
            if (property.isNotEmpty()) {
                holder.textType?.text = (getString(R.string.characteristic) + "( " + property.toString() + ")")
                holder.imageNext?.visibility = View.VISIBLE
            } else {
                holder.imageNext?.visibility = View.INVISIBLE
            }

            return view
        }

        internal inner class ViewHolder {
            var textTitle: TextView? = null
            var textUuid: TextView? = null
            var textType: TextView? = null
            var imageNext: ImageView? = null
        }
    }

}
