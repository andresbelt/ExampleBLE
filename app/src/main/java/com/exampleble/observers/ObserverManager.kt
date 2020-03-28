package com.exampleble.observers

import com.clj.fastble.data.BleDevice

class ObserverManager : Observable {

    private object ObserverManagerHolder {
        val observerManager = ObserverManager()
    }

    private val observers = mutableListOf<Observer?>()

    override fun addObserver(obj: Observer?) {
        observers.add(obj)
    }

    override fun deleteObserver(obj: Observer?) {
        val i = observers.indexOf(obj)
        if (i >= 0) {
            observers.remove(obj)
        }
    }

    override fun notifyObserver(bleDevice: BleDevice?) {
        observers.forEach {
            it?.disconnected(bleDevice)
        }
    }

    companion object {
        val instance: ObserverManager get() = ObserverManagerHolder.observerManager
    }
}