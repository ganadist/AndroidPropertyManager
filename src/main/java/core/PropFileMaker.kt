package core

import java.io.File
import java.io.FileWriter
import java.util.*

/**
 * Created by ganad on 2016-02-17.
 */
class PropFileMaker(var mDeviceManager: DeviceManager) {
  fun makePropFileToPath(path: String, properties: ArrayList<Property>) {
      var fw = FileWriter(path)

      for (property in properties) {
        property.writeLine(fw)
      }
      fw.close()
  }
}