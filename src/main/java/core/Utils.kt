package core

import java.lang.reflect.InvocationTargetException
import javax.swing.SwingUtilities

/**
 * Created by ganad on 2016-02-18.
 */

fun invokeAndWait(runnable: Runnable) {
    if (SwingUtilities.isEventDispatchThread()) {
        runnable.run();
    } else {
        try {
            SwingUtilities.invokeAndWait(runnable);
        } catch (e: InterruptedException) {
            e.printStackTrace();
        } catch (e: InvocationTargetException) {
            e.printStackTrace();
        }
    }
}

fun isEmptyString(str : String?) : Boolean {
    if (str == null) return true
    return str.length == 0
}