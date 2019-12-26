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
import com.desarollobluetooth.fragments.CharacteristicOperationFragment
import com.desarollobluetooth.fragments.MainFragment
import com.exampleble.fragment.CharacteristicListFragment
import com.exampleble.fragment.ServiceListFragment
import com.exampleble.observers.Observer
import com.exampleble.observers.ObserverManager
import java.util.*

class MainActivity : AppCompatActivity(), MainFragment.OnFragmentInteractionListener, Observer {


    private var titles = arrayOfNulls<String>(3)
    private var bleDevice: BleDevice? = null
    private var bluetoothGattService: BluetoothGattService? = null
    private var characteristic: BluetoothGattCharacteristic? = null
    private var charaProp: Int = 0
    private var currentPage = 0
    private val fragments = ArrayList<Fragment>()
    private var toolbar: Toolbar? = null


    override fun disConnected(device: BleDevice?) {
        if (device != null && bleDevice != null && device.key == bleDevice!!.key) {
            //finish()
            Toast.makeText(
                this,
                "Desconnection",
                Toast.LENGTH_LONG
            ).show()
            changePage(1)
        }
    }

    fun changePage(page: Int) {
        currentPage = page
        toolbar!!.title = titles[page]
        updateFragment(page)
        if (currentPage == 2) {
            (fragments[2] as CharacteristicListFragment).showData()
        } else if (currentPage == 3) {
            (fragments[3] as CharacteristicOperationFragment).showData()
        }
    }

    fun initPage() {
       // prepareFragment()
        //onNotify()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout, fragments[0])
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        fragmentTransaction.commit()

    }

    private fun prepareFragment() {

        fragments.add(ServiceListFragment())
        fragments.add(CharacteristicListFragment())
        fragments.add(CharacteristicOperationFragment())

            for (fragment in fragments) {
                if ( fragment !is MainFragment) {
                    supportFragmentManager.beginTransaction().add(R.id.frameLayout, fragment)
                        .hide(fragment).commit()
                }  }
    }


    private fun updateFragment(position: Int) {
        if (position > fragments.size - 1) {
            return
        }
        for (i in fragments.indices) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = fragments[i]
            if (i == position) {
                transaction.show(fragment)
            } else {
                transaction.hide(fragment)
            }
            transaction.commit()
        }
    }

    override fun onFragmentInteraction(bledevice: BleDevice?) {
        this.bleDevice = bledevice
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
        toolbar!!.title = titles[0]
        setSupportActionBar(toolbar)
        fragments.add(MainFragment.newInstance("",""))

        initPage()
        ObserverManager.getInstance().addObserver(this)
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
        ObserverManager.getInstance().deleteObserver(this)
    }

    fun getBleDevice(): BleDevice? {
        return this.bleDevice
    }

    fun getBluetoothGattService(): BluetoothGattService? {
        return bluetoothGattService
    }

    fun setBluetoothGattService(bluetoothGattService: BluetoothGattService) {
        this.bluetoothGattService = bluetoothGattService
    }

    fun getCharacteristic(): BluetoothGattCharacteristic? {
        return characteristic
    }

    fun setCharacteristic(characteristic: BluetoothGattCharacteristic) {
        this.characteristic = characteristic
    }

    fun getCharaProp(): Int {
        return charaProp
    }

    fun setCharaProp(charaProp: Int) {
        this.charaProp = charaProp
    }

}
