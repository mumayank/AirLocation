package mumayank.com.airlocationlibrary.helpers

import android.app.Activity
import android.content.Intent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.lang.ref.WeakReference

class GooglePlayApiHelper(
    private val activity: Activity,
    private val activityWeakReference: WeakReference<Activity>,
    private val onSuccess: (() -> Unit)?,
    private val onFailure: (() -> Unit)?
) {
    fun makeItAvailable() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        val googlePlayServiceAvailability =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity)
        if (googlePlayServiceAvailability == ConnectionResult.SUCCESS) {
            if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                return
            }

            onSuccess?.invoke()
        } else {
            if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                return
            }

            val errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(
                activity,
                googlePlayServiceAvailability,
                REQUEST_CODE
            ) {
                onFailure?.invoke()
            }
            errorDialog.show()
        }

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
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