package com.exampleble.fragment

import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.desarollobluetooth.fragments.CharacteristicOperationFragment
import com.exampleble.MainActivity
import com.exampleble.R
import java.util.ArrayList

class CharacteristicListFragment: Fragment() {


    private var mResultAdapter: ResultAdapter? = null

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
        mResultAdapter = ResultAdapter(context!!)
        val listView_device = v.findViewById(R.id.list_service) as ListView
        listView_device.adapter = mResultAdapter
        listView_device.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val characteristic = mResultAdapter!!.getItem(position)
                val propList = ArrayList<Int>()
                val propNameList = ArrayList<String>()
                val charaProp = characteristic!!.properties
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

                if (propList.size > 1) {
                    AlertDialog.Builder(activity!!)
                        .setTitle(activity!!.getString(R.string.select_operation_type))
                        .setItems(propNameList.toTypedArray(),
                            DialogInterface.OnClickListener { dialog, which ->
                                (getActivity() as MainActivity).setCharacteristic(
                                    characteristic
                                )
                                (getActivity() as MainActivity).setCharaProp(propList[which])
                                (getActivity() as MainActivity).changePage(3)
                            })
                        .show()
                } else if (propList.size > 0) {
                    (getActivity() as MainActivity).setCharacteristic(characteristic)
                    (getActivity() as MainActivity).setCharaProp(propList[0])
                    (activity as MainActivity).changePage(3)
                }
            }
    }

    fun showData() {
        val service = (activity as MainActivity).getBluetoothGattService()
        mResultAdapter!!.clear()
        for (characteristic in service!!.characteristics) {
            mResultAdapter!!.addResult(characteristic)
        }
        mResultAdapter!!.notifyDataSetChanged()
    }

    private inner class ResultAdapter internal constructor(private val context: Context) :
        BaseAdapter() {
        private val characteristicList: MutableList<BluetoothGattCharacteristic>

        init {
            characteristicList = ArrayList()
        }

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
                holder.img_next = convertView.findViewById(R.id.img_next) as ImageView
            }

            val characteristic = characteristicList[position]
            val uuid = characteristic.uuid.toString()

            holder.txt_title!!.setText((getActivity()!!.getString(R.string.characteristic) + "（" + position + ")").toString())
            holder.txt_uuid!!.text = uuid

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
            if (property.length > 0) {
                holder.txt_type!!.setText((getActivity()!!.getString(R.string.characteristic) + "( " + property.toString() + ")").toString())
                holder.img_next!!.visibility = View.VISIBLE
            } else {
                holder.img_next!!.visibility = View.INVISIBLE
            }

            return convertView
        }

        internal inner class ViewHolder {
            var txt_title: TextView? = null
            var txt_uuid: TextView? = null
            var txt_type: TextView? = null
            var img_next: ImageView? = null
        }
    }

}
