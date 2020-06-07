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

        if (permissionList.all {
                ContextCompat.checkSelfPermission(
                    activity,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            onSuccess?.invoke()
        } else {
            ActivityCompat.requestPermissions(
                activityWeakReference.get() as Activity,
                permissionList.toTypedArray(),
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

                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onSuccess?.invoke()
                } else {
                    if (permissionList.any {
                            (ContextCompat.checkSelfPermission(activity, it)
                                    != PackageManager.PERMISSION_GRANTED)
                                    && (ActivityCompat.shouldShowRequestPermissionRationale(
                                activity, it
                            ).not())
                        }) {
                        openAppPermissionSettings(
                            activity,
                            toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied
                        )
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

        private val permissionList = arrayListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        private fun openAppPermissionSettings(
            activity: Activity,
            toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied: String
        ) {
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