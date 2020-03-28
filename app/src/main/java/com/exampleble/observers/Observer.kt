package com.exampleble.observers

import com.clj.fastble.data.BleDevice

interface Observer {
    fun disconnected(bleDevice: BleDevice?)
}