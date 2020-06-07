package mumayank.com.airlocationlibrary.helpers

import android.app.Activity
import java.lang.ref.WeakReference

class ActivityHelper {

    companion object {

        fun isActivityWeakReferenceNull(activityWeakReference: WeakReference<Activity>): Boolean {
            return activityWeakReference.get() == null
        }

    }

}