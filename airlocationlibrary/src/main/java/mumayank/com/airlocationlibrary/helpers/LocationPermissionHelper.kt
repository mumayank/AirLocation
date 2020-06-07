package mumayank.com.airlocationlibrary.helpers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

class LocationPermissionHelper(
    private val activity: Activity,
    private val activityWeakReference: WeakReference<Activity>,
    private val onSuccess: (() -> Unit)?,
    private val onFailure: (() -> Unit)?,
    private val toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied: String
) {
    fun getPermissions() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        if (isPermissionListGranted(activity, getPermissionList())) {
            onSuccess?.invoke()
        } else {
            ActivityCompat.requestPermissions(
                activityWeakReference.get() as Activity,
                getPermissionList().toTypedArray(),
                REQUEST_CODE
            )
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isEmpty()) {
                    onFailure?.invoke()
                    return
                }

                var granted = true
                for (grantResult in grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        granted = false
                        break
                    }
                }

                if (granted) {
                    onSuccess?.invoke()
                } else {
                    if (isAnyPermissionPermanentlyDisabled(activity, getPermissionList())) {
                        openAppPermissionSettings(activity, toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied)
                    } else {
                        // intentonally ignored because no need to additionally also open settings page
                        // as user hasn't permanently disabled the settings yet
                    }
                    onFailure?.invoke()
                }
            }
        }

    }

    companion object {

        private const val REQUEST_CODE = 1236

        private fun getPermissionList(): ArrayList<String> {
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            return permissions
        }

        private fun isPermissionListGranted(
            activity: Activity,
            permissions: ArrayList<String>
        ): Boolean {
            var isGranted = true
            for (permission in permissions) {
                val isThisPermissionGranted = ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
                if (isThisPermissionGranted.not()) {
                    isGranted = false
                    break
                }
            }
            return isGranted
        }

        private fun isAnyPermissionPermanentlyDisabled(
            activity: Activity,
            permissions: ArrayList<String>
        ): Boolean {
            var isPermanentlyDisabled = false
            for (permission in permissions) {
                val isPermissionNotGranted = ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
                val isPermissionPermanentlyDisabled =
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, permission).not()
                if (isPermissionNotGranted && isPermissionPermanentlyDisabled) {
                    isPermanentlyDisabled = true
                    break
                }
            }
            return isPermanentlyDisabled
        }

        private fun openAppPermissionSettings(activity: Activity, toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied: String) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(intent)
            Toast.makeText(
                activity,
                toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}