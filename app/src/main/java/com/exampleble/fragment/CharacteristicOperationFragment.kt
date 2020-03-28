package com.exampleble.fragment

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
import androidx.core.view.forEach
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

class CharacteristicOperationFragment: Fragment() {

    companion object {
        const val PROPERTY_READ = 1
        const val PROPERTY_WRITE = 2
        const val PROPERTY_WRITE_NO_RESPONSE = 3
        const val PROPERTY_NOTIFY = 4
        const val PROPERTY_INDICATE = 5
    }

    private var layoutContainer: LinearLayout? = null
    private val childList = mutableListOf<String>()

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
        layoutContainer = v.findViewById(R.id.layout_container) as LinearLayout
    }

    fun showData() {
        with(activity as MainActivity) {
            val child = characteristic?.uuid.toString() + charaProp.toString()

            layoutContainer?.forEach { container ->
                container.visibility = View.GONE
            }
            if (childList.contains(child)) {
                layoutContainer?.findViewWithTag<View>(bleDevice?.key + characteristic?.uuid.toString() + charaProp)
                    ?.visibility = View.VISIBLE
            } else {
                childList.add(child)

                val view = LayoutInflater.from(this).inflate(R.layout.layout_characteric_operation, null)
                view.tag = bleDevice?.key + characteristic?.uuid.toString() + charaProp
                val layoutAdd = view.findViewById<LinearLayout>(R.id.layout_add)
                val textTitle = view.findViewById<TextView>(R.id.text_title)
                textTitle.text = characteristic?.uuid.toString() + getString(R.string.data_changed)
                val text = view.findViewById<TextView>(R.id.txt)
                text.movementMethod = ScrollingMovementMethod.getInstance()

                when (charaProp) {
                    PROPERTY_READ -> {
                        val viewAdd = LayoutInflater.from(this).inflate(R.layout.layout_characteric_operation_button, null)
                        val button = viewAdd.findViewById<Button>(R.id.button)
                        button.text = getString(R.string.read)
                        button.setOnClickListener {
                            BleManager.getInstance().read(
                                bleDevice,
                                characteristic?.service?.uuid.toString(),
                                characteristic?.uuid.toString(),
                                object : BleReadCallback() {
                                    override fun onReadSuccess(data: ByteArray) {
                                        runOnUiThread {
                                            addText(
                                                text,
                                                HexUtil.formatHexString(data, true)
                                            )
                                        }
                                    }

                                    override fun onReadFailure(exception: BleException) {
                                        runOnUiThread {
                                            addText(
                                                text,
                                                exception.toString()
                                            )
                                        }
                                    }
                                })
                        }
                        layoutAdd.addView(viewAdd)
                    }

                    PROPERTY_WRITE -> {
                        val viewAdd = LayoutInflater.from(this).inflate(R.layout.layout_characteric_operation_et, null)
                        val editText = viewAdd.findViewById<EditText>(R.id.editText)
                        val button = viewAdd.findViewById<Button>(R.id.button)
                        button.text = getString(R.string.write)
                        button.setOnClickListener(View.OnClickListener {
                            val hex = editText.text.toString()
                            if (TextUtils.isEmpty(hex)) {
                                return@OnClickListener
                            }
                            BleManager.getInstance().write(
                                bleDevice,
                                characteristic?.service?.uuid.toString(),
                                characteristic?.uuid.toString(),
                                HexUtil.hexStringToBytes(hex),
                                object : BleWriteCallback() {
                                    override fun onWriteSuccess(
                                        current: Int,
                                        total: Int,
                                        justWrite: ByteArray
                                    ) {
                                        runOnUiThread {
                                            addText(
                                                text, "write success, current: " + current
                                                        + " total: " + total
                                                        + " justWrite: " + HexUtil.formatHexString(
                                                    justWrite,
                                                    true
                                                )
                                            )
                                        }
                                    }

                                    override fun onWriteFailure(exception: BleException) {
                                        runOnUiThread {
                                            addText(
                                                text,
                                                exception.toString()
                                            )
                                        }
                                    }
                                })
                        })
                        layoutAdd.addView(viewAdd)
                    }

                    PROPERTY_WRITE_NO_RESPONSE -> {
                        val viewAdd = LayoutInflater.from(this).inflate(R.layout.layout_characteric_operation_et, null)
                        val editText = viewAdd.findViewById<EditText>(R.id.editText)
                        val button = viewAdd.findViewById<Button>(R.id.button)
                        button.text = getString(R.string.write)
                        button.setOnClickListener(View.OnClickListener {
                            val hex = editText.text.toString()
                            if (TextUtils.isEmpty(hex)) {
                                return@OnClickListener
                            }
                            BleManager.getInstance().write(
                                bleDevice,
                                characteristic?.service?.uuid.toString(),
                                characteristic?.uuid.toString(),
                                HexUtil.hexStringToBytes(hex),
                                object : BleWriteCallback() {
                                    override fun onWriteSuccess(
                                        current: Int,
                                        total: Int,
                                        justWrite: ByteArray
                                    ) {
                                        runOnUiThread {
                                            addText(
                                                text, "write success, current: " + current
                                                        + " total: " + total
                                                        + " justWrite: " + HexUtil.formatHexString(
                                                    justWrite,
                                                    true
                                                )
                                            )
                                        }
                                    }

                                    override fun onWriteFailure(exception: BleException) {
                                        runOnUiThread {
                                            addText(
                                                text,
                                                exception.toString()
                                            )
                                        }
                                    }
                                })
                        })
                        layoutAdd.addView(viewAdd)
                    }

                    PROPERTY_NOTIFY -> {
                        val viewAdd = LayoutInflater.from(this).inflate(R.layout.layout_characteric_operation_button, null)
                        val button = viewAdd.findViewById<Button>(R.id.button)
                        button.text = getString(R.string.open_notification)
                        button.setOnClickListener {
                            if (button.text.toString() == getString(R.string.open_notification)) {
                                button.text = getString(R.string.close_notification)
                                BleManager.getInstance().notify(
                                    bleDevice,
                                    characteristic?.service?.uuid.toString(),
                                    characteristic?.uuid.toString(),
                                    object : BleNotifyCallback() {

                                        override fun onNotifySuccess() {
                                            runOnUiThread {
                                                addText(
                                                    text,
                                                    "notify success"
                                                )
                                            }
                                        }

                                        override fun onNotifyFailure(exception: BleException) {
                                            runOnUiThread {
                                                addText(
                                                    text,
                                                    exception.toString()
                                                )
                                            }
                                        }

                                        override fun onCharacteristicChanged(data: ByteArray) {
                                            runOnUiThread {
                                                addText(
                                                    text,
                                                    HexUtil.formatHexString(
                                                        characteristic?.value,
                                                        true
                                                    )
                                                )
                                            }
                                        }
                                    })
                            } else {
                                button.text = getString(R.string.open_notification)
                                BleManager.getInstance().stopNotify(
                                    bleDevice,
                                    characteristic?.service?.uuid.toString(),
                                    characteristic?.uuid.toString()
                                )
                            }
                        }
                        layoutAdd.addView(viewAdd)
                    }

                    PROPERTY_INDICATE -> {
                        val viewAdd = LayoutInflater.from(this).inflate(R.layout.layout_characteric_operation_button, null)
                        val button = viewAdd.findViewById<Button>(R.id.button)
                        button.text = getString(R.string.open_notification)
                        button.setOnClickListener {
                            if (button.text.toString() == getString(R.string.open_notification)) {
                                button.text = getString(R.string.close_notification)
                                BleManager.getInstance().indicate(
                                    bleDevice,
                                    characteristic?.service?.uuid.toString(),
                                    characteristic?.uuid.toString(),
                                    object : BleIndicateCallback() {

                                        override fun onIndicateSuccess() {
                                            runOnUiThread {
                                                addText(
                                                    text,
                                                    "indicate success"
                                                )
                                            }
                                        }

                                        override fun onIndicateFailure(exception: BleException) {
                                            runOnUiThread {
                                                addText(
                                                    text,
                                                    exception.toString()
                                                )
                                            }
                                        }

                                        override fun onCharacteristicChanged(data: ByteArray) {
                                            runOnUiThread {
                                                addText(
                                                    text,
                                                    HexUtil.formatHexString(
                                                        characteristic?.value,
                                                        true
                                                    )
                                                )
                                            }
                                        }
                                    })
                            } else {
                                button.text = getString(R.string.open_notification)
                                BleManager.getInstance().stopIndicate(
                                    bleDevice,
                                    characteristic?.service?.uuid.toString(),
                                    characteristic?.uuid.toString()
                                )
                            }
                        }
                        layoutAdd.addView(viewAdd)
                    }
                }

                layoutContainer?.addView(view)
            }
        }
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
