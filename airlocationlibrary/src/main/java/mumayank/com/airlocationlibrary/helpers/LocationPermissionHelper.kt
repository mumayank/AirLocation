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
    activity: Activity,
    private val onSuccess: (() -> Unit)?,
    private val onFailure: (() -> Unit)?,
    private val toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied: String
) {
    private val activityWeakReference = WeakReference(activity)

    fun getPermissions() {
        val activityTemp = activityWeakReference.get() ?: return

        if (permissionList.all {
                ContextCompat.checkSelfPermission(
                    activityTemp,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            onSuccess?.invoke()
        } else {
            ActivityCompat.requestPermissions(
                activityTemp,
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
        val activityTemp = activityWeakReference.get() ?: return

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
                            (ContextCompat.checkSelfPermission(activityTemp, it)
                                    != PackageManager.PERMISSION_GRANTED)
                                    && (ActivityCompat.shouldShowRequestPermissionRationale(
                                activityTemp, it
                            ).not())
                        }) {
                        openAppPermissionSettings(
                            activityTemp,
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