package mumayank.com.airlocationlibrary.helpers

import android.app.Activity
import android.content.Intent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.lang.ref.WeakReference

class GooglePlayApiHelper(
    activity: Activity,
    private val onSuccess: (() -> Unit)?,
    private val onFailure: (() -> Unit)?
) {
    private val activityWeakReference = WeakReference(activity)

    fun makeItAvailable() {
        val activityTemp = activityWeakReference.get() ?: return

        val googlePlayServiceAvailability =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activityTemp)
        if (googlePlayServiceAvailability == ConnectionResult.SUCCESS) {
            if (activityWeakReference.get() == null) {
                return
            }

            onSuccess?.invoke()
        } else {
            val activityTemp2 = activityWeakReference.get() ?: return

            val errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(
                activityTemp2,
                googlePlayServiceAvailability,
                REQUEST_CODE
            ) {
                onFailure?.invoke()
            }
            errorDialog.show()
        }

    }

    fun onActivityResult(requestCode: Int) {
        if (activityWeakReference.get() == null) {
            return
        }

        if (requestCode == REQUEST_CODE) {
            makeItAvailable()
        }
    }

    companion object {
        private const val REQUEST_CODE = 1237
    }
}