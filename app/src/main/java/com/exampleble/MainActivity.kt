package com.exampleble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.clj.fastble.BleManager
import com.clj.fastble.data.BleDevice
import com.exampleble.fragment.CharacteristicListFragment
import com.exampleble.fragment.CharacteristicOperationFragment
import com.exampleble.fragment.MainFragment
import com.exampleble.fragment.ServiceListFragment
import com.exampleble.observers.Observer
import com.exampleble.observers.ObserverManager

class MainActivity : AppCompatActivity(), MainFragment.OnFragmentInteractionListener, Observer {

    private var titles = arrayOfNulls<String>(3)

    var bleDevice: BleDevice? = null
        private set

    var bluetoothGattService: BluetoothGattService? = null
    var characteristic: BluetoothGattCharacteristic? = null
    var charaProp: Int = 0
    private var currentPage = 0
    private val fragments = mutableListOf<Fragment>()
    private var toolbar: Toolbar? = null

    override fun disconnected(device: BleDevice?) {
        if (device != null && bleDevice != null && device.key == bleDevice?.key) {
            Toast.makeText(this, "Disconnection", Toast.LENGTH_LONG).show()
            changePage(1)
        }
    }

    fun changePage(page: Int) {
        currentPage = page
        toolbar?.title = titles[page]
        updateFragment(page)
        if (currentPage == 2) {
            (fragments[2] as CharacteristicListFragment).showData()
        } else if (currentPage == 3) {
            (fragments[3] as CharacteristicOperationFragment).showData()
        }
    }

    private fun initPage() {
        with(supportFragmentManager.beginTransaction()) {
            replace(R.id.frameLayout, fragments[0])
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            commit()
        }
    }

    private fun prepareFragment() {
        with(fragments) {
            add(ServiceListFragment())
            add(CharacteristicListFragment())
            add(CharacteristicOperationFragment())

            forEach { fragment ->
                if (fragment !is MainFragment) {
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.frameLayout, fragment)
                        .hide(fragment)
                        .commit()
                }
            }
        }
    }

    private fun updateFragment(position: Int) {
        if (position > fragments.size - 1) {
            return
        }
        fragments.forEachIndexed { index, fragment ->
            val transaction = supportFragmentManager.beginTransaction()
            if (index == position) {
                transaction.show(fragment)
            } else {
                transaction.hide(fragment)
            }
            transaction.commit()
        }
    }

    override fun onFragmentInteraction(bleDevice: BleDevice?) {
        this.bleDevice = bleDevice
        prepareFragment()
        changePage(1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_bar_main)

        titles = arrayOf(
            getString(R.string.service_list),
            getString(R.string.characteristic_list),
            getString(R.string.console),
            getString(R.string.console)
        )

        toolbar = findViewById(R.id.toolbar)
        toolbar?.title = titles[0]
        setSupportActionBar(toolbar)
        fragments.add(MainFragment.newInstance("",""))

        initPage()
        ObserverManager.instance.addObserver(this)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentPage != 0) {
                currentPage--
                changePage(currentPage)
                true
            } else {
                true
            }
        } else super.onKeyDown(keyCode, event)
    }


    override fun onDestroy() {
        super.onDestroy()
        BleManager.getInstance().clearCharacterCallback(bleDevice)
        ObserverManager.instance.deleteObserver(this)
    }
}
