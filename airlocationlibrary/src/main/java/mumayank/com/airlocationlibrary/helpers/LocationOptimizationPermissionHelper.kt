package mumayank.com.airlocationlibrary.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import mumayank.com.airlocationlibrary.AirLocation
import java.lang.ref.WeakReference

class LocationOptimizationPermissionHelper(
    activity: Activity,
    private val locationInterval: Long,
    private val isLocationRequiredOnlyOneTime: Boolean,
    private val onSuccess: (() -> Unit)?,
    private val onFailure: ((locationFailedEnum: AirLocation.LocationFailedEnum) -> Unit)?
) {
    private val activityWeakReference = WeakReference(activity)

    fun getPermission() {
        val activityTemp = activityWeakReference.get() ?: return

        // check current location settings
        (LocationServices.getSettingsClient(activityTemp)
            .checkLocationSettings(
                (LocationSettingsRequest.Builder().addLocationRequest(
                    getLocationRequest(locationInterval, isLocationRequiredOnlyOneTime)
                )).build()
            ).addOnSuccessListener { locationSettingsResponse ->
                if (activityWeakReference.get() == null) {
                    return@addOnSuccessListener
                }

                onSuccess?.invoke()
            }.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    val activityTemp2 = activityWeakReference.get() ?: return@addOnFailureListener

                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                        exception.startResolutionForResult(
                            activityTemp2,
                            REQUEST_CODE
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        onFailure?.invoke(AirLocation.LocationFailedEnum.COULD_NOT_OPTIMIZE_DEVICE_HARDWARE)
                    }
                } else {
                    if (activityWeakReference.get() == null) {
                        return@addOnFailureListener
                    }

                    onFailure?.invoke(AirLocation.LocationFailedEnum.COULD_NOT_OPTIMIZE_DEVICE_HARDWARE)
                }
            })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val activityTemp = activityWeakReference.get() ?: return

        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                onSuccess?.invoke()
            } else {
                val locationManager =
                    activityTemp.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (activityWeakReference.get() == null) {
                        return
                    }

                    onFailure?.invoke(AirLocation.LocationFailedEnum.HIGH_PRECISION_LOCATION_NA_TRY_AGAIN_PREFERABLY_WITH_NETWORK_CONNECTIVITY)
                } else {
                    if (activityWeakReference.get() == null) {
                        return
                    }

                    onFailure?.invoke(AirLocation.LocationFailedEnum.LOCATION_OPTIMIZATION_PERMISSION_NOT_GRANTED)
                }
            }
        }
    }


    companion object {
        const val REQUEST_CODE = 1235

        fun getLocationRequest(
            locationInterval: Long,
            isLocationRequiredOnlyOneTime: Boolean
        ): LocationRequest {
            return LocationRequest.create().apply {
                interval = locationInterval
                fastestInterval = locationInterval
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                if (isLocationRequiredOnlyOneTime) numUpdates = 1
            }
        }
    }

}