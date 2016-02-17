package core

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.ui.Messages
import exception.NullAndroidHomeException
import java.io.File
import java.util.*

/**
 * Created by ganad on 2016-02-17.
 */
public class DeviceManager : AndroidDebugBridge.IDeviceChangeListener {


    var mProjectManager = ProjectManager.getInstance();
    var mProject = mProjectManager.getDefaultProject()
    var mAdb: AndroidDebugBridge? = null
    var mAdbPath : String? = null
    var mDevice: Device? = null
    var mDeviceMap = HashMap<String, Device>()

    public fun adbInit() {
        AndroidDebugBridge.initIfNeeded(false)

        var androidHome = findAndroidSdkHome()
        mAdbPath = getAdbLocation(androidHome).path
        mAdb = AndroidDebugBridge.createBridge(mAdbPath, true)
        AndroidDebugBridge.addDeviceChangeListener(this)
    }

    private fun getAdbLocation(androidHome: String?) : File {
        var paths = arrayOf(androidHome, "platform-tools", "adb")
        return File(paths.joinToString(File.separator))
    }

    private fun findAndroidSdkHome(): String? {
        for (sdk in ProjectJdkTable.getInstance().allJdks.filter {
                    it.sdkType.name == "Android SDK" && getAdbLocation(it.homePath).exists()
                }) {
            return sdk.homePath
        }

        var envSdkHome = System.getenv("ANDROID_HOME")
        if (getAdbLocation(envSdkHome).exists()) {
            return envSdkHome
        }

        Messages.showMessageDialog(mProject, "Cannot Find \$ANDROID_HOME and Android SDK\n" +
                "Please set Android Sdk or \$ANDROID_HOME and restart", "Android Property Manager",
                Messages.getInformationIcon())

        throw NullAndroidHomeException()
    }

    fun putProperty(name: String, property: Property) {

    }

    fun getPropertyNames() : ArrayList<String> {
        return ArrayList<String>();
    }

    fun getProperty(name: String): Property? {
        return null
    }

    fun setPropertyValue(name: String, value: String) {

    }

    fun getConnectedDeviceNameList(): ArrayList<String>? {
        return null
    }

    fun changeDevice(name: String): Device? {
        mDevice = mDeviceMap.get(name)
        return mDevice
    }

    fun getDevice() : Device? {
        return mDevice
    }

    fun updatePropFromDevice() {

    }

    fun setRootMode() {

    }

    fun getCurrentDeviceState(): Int {
        return 0;
    }

    fun restartRuntime() {

    }

    fun savePropFile(path: String, propertyNames: ArrayList<String>) {

    }

    fun pushPropFile(path: String, oldPropDirPath: String) {

    }

    fun loadPropFile(path: String) {

    }

    fun rebootDevice() {

    }

    override fun deviceDisconnected(device: IDevice?) {
        var device = mDeviceMap.remove(device!!.serialNumber)
        device!!.updateDeviceState(Device.State.PROPERTY_INVISIBLE)
    }

    override fun deviceConnected(device: IDevice?) {
        var device = Device(device!!, mAdbPath!!)
        mDeviceMap.put(device!!.getSerialNumber(), device)
    }

    override fun deviceChanged(device: IDevice?, state: Int) {

    }

    interface DeviceChangeListener {
        fun onDeviceAdded(name: String)
        fun onDeviceRemoved(name: String)
    }
}

var manager: DeviceManager? = null
fun getDeviceManagerInstance(): DeviceManager? {
    if (manager == null) {
        manager = DeviceManager()
    }
    return manager
}