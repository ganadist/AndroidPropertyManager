package core

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.*

/**
 * Created by ganad on 2016-02-17.
 */
public class Property (var mName : String, var mValue: String) {
    var mValueHistory = ArrayList<String>()
    fun getName(): String {
        return mName;
    }

    fun getValue(): String {
        return mValue;
    }

    fun setValue(value: String) {
        mValue = value;
        mValueHistory.add(value)
    }

    fun writeLine(osw: OutputStreamWriter) {
        osw.write(mName)
        osw.write("=")
        osw.write(mValue)
        osw.write("\n")
    }
}