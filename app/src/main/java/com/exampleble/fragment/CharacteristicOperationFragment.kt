package com.desarollobluetooth.fragments

import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleIndicateCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.callback.BleReadCallback
import com.clj.fastble.callback.BleWriteCallback
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.exampleble.MainActivity
import com.exampleble.R
import java.util.ArrayList

class CharacteristicOperationFragment: Fragment() {

    companion object {
        const val PROPERTY_READ = 1
        const val PROPERTY_WRITE = 2
        const val PROPERTY_WRITE_NO_RESPONSE = 3
        const val PROPERTY_NOTIFY = 4
        const val PROPERTY_INDICATE = 5
    }

    private var layout_container: LinearLayout? = null
    private val childList = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_characteric_operation, null)
        initView(v)
        return v
    }

    private fun initView(v: View) {
        layout_container = v.findViewById(R.id.layout_container) as LinearLayout
    }

    fun showData() {
        val bleDevice = (getActivity() as MainActivity).getBleDevice()
        val characteristic = (getActivity() as MainActivity).getCharacteristic()
        val charaProp = (getActivity() as MainActivity).getCharaProp()
        val child = characteristic!!.uuid.toString() + charaProp.toString()

        for (i in 0 until layout_container!!.childCount) {
            layout_container!!.getChildAt(i).visibility = View.GONE
        }
        if (childList.contains(child)) {
            layout_container!!.findViewWithTag<View>(bleDevice!!.key + characteristic!!.uuid.toString() + charaProp)
                .visibility = View.VISIBLE
        } else {
            childList.add(child)

            val view = LayoutInflater.from(getActivity())
                .inflate(R.layout.layout_characteric_operation, null)
            view.setTag(bleDevice!!.key + characteristic!!.uuid.toString() + charaProp)
            val layout_add = view.findViewById(R.id.layout_add) as LinearLayout
            val txt_title = view.findViewById(R.id.txt_title) as TextView
            txt_title.text =
                characteristic.getUuid().toString() + getActivity()!!.getString(R.string.data_changed)
            val txt = view.findViewById(R.id.txt) as TextView
            txt.movementMethod = ScrollingMovementMethod.getInstance()

            when (charaProp) {
                PROPERTY_READ -> {
                    val view_add = LayoutInflater.from(getActivity())
                        .inflate(R.layout.layout_characteric_operation_button, null)
                    val btn = view_add.findViewById(R.id.btn) as Button
                    btn.setText(getActivity()!!.getString(R.string.read))
                    btn.setOnClickListener {
                        BleManager.getInstance().read(
                            bleDevice,
                            characteristic.getService().uuid.toString(),
                            characteristic.getUuid().toString(),
                            object : BleReadCallback() {

                                override fun onReadSuccess(data: ByteArray) {
                                    runOnUiThread(Runnable {
                                        addText(
                                            txt,
                                            HexUtil.formatHexString(data, true)
                                        )
                                    })
                                }

                                override fun onReadFailure(exception: BleException) {
                                    runOnUiThread(Runnable { addText(txt, exception.toString()) })
                                }
                            })
                    }
                    layout_add.addView(view_add)
                }

                PROPERTY_WRITE -> {
                    val view_add = LayoutInflater.from(getActivity())
                        .inflate(R.layout.layout_characteric_operation_et, null)
                    val et = view_add.findViewById(R.id.et) as EditText
                    val btn = view_add.findViewById(R.id.btn) as Button
                    btn.setText(getActivity()!!.getString(R.string.write))
                    btn.setOnClickListener(View.OnClickListener {
                        val hex = et.text.toString()
                        if (TextUtils.isEmpty(hex)) {
                            return@OnClickListener
                        }
                        BleManager.getInstance().write(
                            bleDevice,
                            characteristic.getService().uuid.toString(),
                            characteristic.getUuid().toString(),
                            HexUtil.hexStringToBytes(hex),
                            object : BleWriteCallback() {

                                override fun onWriteSuccess(
                                    current: Int,
                                    total: Int,
                                    justWrite: ByteArray
                                ) {
                                    runOnUiThread(Runnable {
                                        addText(
                                            txt, "write success, current: " + current
                                                    + " total: " + total
                                                    + " justWrite: " + HexUtil.formatHexString(
                                                justWrite,
                                                true
                                            )
                                        )
                                    })
                                }

                                override fun onWriteFailure(exception: BleException) {
                                    runOnUiThread(Runnable { addText(txt, exception.toString()) })
                                }
                            })
                    })
                    layout_add.addView(view_add)
                }

                PROPERTY_WRITE_NO_RESPONSE -> {
                    val view_add = LayoutInflater.from(getActivity())
                        .inflate(R.layout.layout_characteric_operation_et, null)
                    val et = view_add.findViewById(R.id.et) as EditText
                    val btn = view_add.findViewById(R.id.btn) as Button
                    btn.setText(getActivity()!!.getString(R.string.write))
                    btn.setOnClickListener(View.OnClickListener {
                        val hex = et.text.toString()
                        if (TextUtils.isEmpty(hex)) {
                            return@OnClickListener
                        }
                        BleManager.getInstance().write(
                            bleDevice,
                            characteristic.getService().uuid.toString(),
                            characteristic.getUuid().toString(),
                            HexUtil.hexStringToBytes(hex),
                            object : BleWriteCallback() {

                                override fun onWriteSuccess(
                                    current: Int,
                                    total: Int,
                                    justWrite: ByteArray
                                ) {
                                    runOnUiThread(Runnable {
                                        addText(
                                            txt, "write success, current: " + current
                                                    + " total: " + total
                                                    + " justWrite: " + HexUtil.formatHexString(
                                                justWrite,
                                                true
                                            )
                                        )
                                    })
                                }

                                override fun onWriteFailure(exception: BleException) {
                                    runOnUiThread(Runnable { addText(txt, exception.toString()) })
                                }
                            })
                    })
                    layout_add.addView(view_add)
                }

                PROPERTY_NOTIFY -> {
                    val view_add = LayoutInflater.from(getActivity())
                        .inflate(R.layout.layout_characteric_operation_button, null)
                    val btn = view_add.findViewById(R.id.btn) as Button
                    btn.setText(getActivity()!!.getString(R.string.open_notification))
                    btn.setOnClickListener {
                        if (btn.text.toString() == getActivity()!!.getString(R.string.open_notification)) {
                            btn.setText(getActivity()!!.getString(R.string.close_notification))
                            BleManager.getInstance().notify(
                                bleDevice,
                                characteristic.service.uuid.toString(),
                                characteristic.uuid.toString(),
                                object : BleNotifyCallback() {

                                    override fun onNotifySuccess() {
                                        runOnUiThread(Runnable { addText(txt, "notify success") })
                                    }

                                    override fun onNotifyFailure(exception: BleException) {
                                        runOnUiThread(Runnable {
                                            addText(
                                                txt,
                                                exception.toString()
                                            )
                                        })
                                    }

                                    override fun onCharacteristicChanged(data: ByteArray) {
                                        runOnUiThread(Runnable {
                                            addText(
                                                txt,
                                                HexUtil.formatHexString(
                                                    characteristic.getValue(),
                                                    true
                                                )
                                            )
                                        })
                                    }
                                })
                        } else {
                            btn.text = getActivity()!!.getString(R.string.open_notification)
                            BleManager.getInstance().stopNotify(
                                bleDevice,
                                characteristic!!.service.uuid.toString(),
                                characteristic!!.uuid.toString()
                            )
                        }
                    }
                    layout_add.addView(view_add)
                }

                PROPERTY_INDICATE -> {
                    val view_add = LayoutInflater.from(getActivity())
                        .inflate(R.layout.layout_characteric_operation_button, null)
                    val btn = view_add.findViewById(R.id.btn) as Button
                    btn.setText(getActivity()!!.getString(R.string.open_notification))
                    btn.setOnClickListener {
                        if (btn.text.toString() == getActivity()!!.getString(R.string.open_notification)) {
                            btn.setText(getActivity()!!.getString(R.string.close_notification))
                            BleManager.getInstance().indicate(
                                bleDevice,
                                characteristic.getService().uuid.toString(),
                                characteristic.getUuid().toString(),
                                object : BleIndicateCallback() {

                                    override fun onIndicateSuccess() {
                                        runOnUiThread(Runnable { addText(txt, "indicate success") })
                                    }

                                    override fun onIndicateFailure(exception: BleException) {
                                        runOnUiThread(Runnable {
                                            addText(
                                                txt,
                                                exception.toString()
                                            )
                                        })
                                    }

                                    override fun onCharacteristicChanged(data: ByteArray) {
                                        runOnUiThread(Runnable {
                                            addText(
                                                txt,
                                                HexUtil.formatHexString(
                                                    characteristic!!.value,
                                                    true
                                                )
                                            )
                                        })
                                    }
                                })
                        } else {
                            btn.setText(getActivity()!!.getString(R.string.open_notification))
                            BleManager.getInstance().stopIndicate(
                                bleDevice,
                                characteristic!!.service.uuid.toString(),
                                characteristic!!.uuid.toString()
                            )
                        }
                    }
                    layout_add.addView(view_add)
                }
            }

            layout_container!!.addView(view)
        }
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (isAdded() && getActivity() != null)
            getActivity()!!.runOnUiThread(runnable)
    }

    private fun addText(textView: TextView, content: String) {
        textView.append(content)
        textView.append("\n")
        val offset = textView.lineCount * textView.lineHeight
        if (offset > textView.height) {
            textView.scrollTo(0, offset - textView.height)
        }
    }

}
