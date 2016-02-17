package core

import com.android.ddmlib.*
import com.intellij.util.containers.HashMap


import java.io.*
import java.util.ArrayList

/**
 * Created by ganad on 2016-02-16.
 */
class Device (var mIDevice : IDevice, var mAdbPath: String) {
    private var mProperties = HashMap<String, Property>()
    private var mSerial = mIDevice.getSerialNumber()
    init {
        queryRoot()
    }

    enum class State {
        PROPERTY_EDITABLE, PROPERTY_VISIBLE, PROPERTY_INVISIBLE
    }

    fun setRootMode() {
        executeAdbCommand("root")
    }

    fun isRootMode(): Boolean {
        return false
    }

    fun getState(): State {
        if (mIDevice.getState() == IDevice.DeviceState.ONLINE) {
            if (isRootMode()) {
                return State.PROPERTY_EDITABLE
            } else {
                return State.PROPERTY_VISIBLE
            }
        }
        return State.PROPERTY_INVISIBLE
    }

    fun putProperty(name: String, property: Property) {
        mProperties.put(name, property)
    }

    fun getProperty(name: String): Property {
        return mProperties.getOrDefault(name, null)
    }

    fun setPropertyValue(name: String, value: String) {
        var setPropCommand : Array<String> = arrayOf("setprop", name, value)
        executeShellCommand(setPropCommand.joinToString { " " } )
    }

    fun getSerialNumber(): String {
        return mSerial
    }

    private fun executeShellCommand(command : String) {
        executeShellCommand(command, NullOutputListener())
    }

    private fun executeShellCommand(command: String, receiver: ShellOutputListener) {
        mIDevice.executeShellCommand(command, object: MultiLineReceiver() {
            override fun processNewLines(lines: Array<out String>?) {
                for (line in lines!!.asIterable()) {
                    if (isEmptyString(line)) {
                        continue
                    }
                    invokeAndWait(object: Runnable {
                        override fun run() {
                            receiver.onOutput(line)
                        }
                    })
                }
            }

            override fun isCancelled(): Boolean {
                return receiver.isCancelled()
            }
        });
    }

    private fun executeAdbCommand(command: String) {
        var builder = ProcessBuilder()
        builder.command(mAdbPath, "-s", mSerial, command)
        // FIXME
        builder.start().waitFor()

    }

    fun remount() {
        executeAdbCommand("remount")
    }

    fun reboot(reason: String) {
        mIDevice.reboot(reason)
    }

    fun restart() {
        executeShellCommand("stop")
        executeShellCommand("start")
    }

    fun pushFile(localName: String, remoteName: String) {
        mIDevice.pushFile(localName, remoteName)
    }

    fun pullFile(localName: String, remoteName: String) {
        mIDevice.pullFile(localName, remoteName)
    }

    fun getPropertyNames(): ArrayList<String> {
        return ArrayList<String>(mProperties.keys)
    }

    private var mPropertyMonitoring = false
    fun startPropertyMonitoring(listener: PropertyChangeListener) {
        mPropertyMonitoring = true

        executeShellCommand("watchprops", object : ShellOutputListener {
            override fun isCancelled(): Boolean {
                return !mPropertyMonitoring
            }

            override fun onOutput(line: String) {
                var parsed = line.split(' ')
                var name = parsed[1]
                var value = parsed[3].substring(1, parsed[3].length - 1) // strip ''
                listener.onPropertyChanged(name, value)
            }
        })

        executeShellCommand("getprop", object : ShellOutputListener {
            override fun isCancelled(): Boolean {
                return !mPropertyMonitoring
            }

            override fun onOutput(line: String) {
                var parsed = line.split(':')
                var name = parsed[0].substring(1, parsed[0].length - 1) // strip []
                var value = parsed[1].substring(2, parsed[1].length - 1) // strip []
                listener.onPropertyChanged(name, value)
            }
        })
    }

    fun stopPropetyMonitoring() {
        mPropertyMonitoring = false
    }

    private var mStateListener: StateChangeListener? = null
    private var mDeviceState = State.PROPERTY_VISIBLE

    fun setStateChangeListener(listener: StateChangeListener) {
        mStateListener = listener
        listener.onStateChanged(mDeviceState)
    }

    fun updateDeviceState(state: State) {
        if (mDeviceState != state) {
            mDeviceState = state
            mStateListener!!.onStateChanged(state)
        }
    }

    fun queryRoot() {
        executeShellCommand("id -u", object : ShellOutputListener {
            override fun onOutput(line: String) {
                if (mIDevice.state == IDevice.DeviceState.ONLINE) {
                    if (line == "0") {
                        updateDeviceState(State.PROPERTY_EDITABLE)
                    } else {
                        updateDeviceState(State.PROPERTY_VISIBLE)
                    }
                } else {
                    updateDeviceState(State.PROPERTY_INVISIBLE)
                }
            }
        })
    }

    interface PropertyChangeListener {
        fun onPropertyChanged(name:String, value:String)
    }

    interface StateChangeListener {
        fun onStateChanged(state: Device.State)
    }

    interface ShellOutputListener {
        fun onOutput(line: String)
        fun isCancelled(): Boolean { return false }
    }

    class NullOutputListener : ShellOutputListener {
        override fun onOutput(line: String) {}
    }
}