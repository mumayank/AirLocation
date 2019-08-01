package mumayank.com.airlocationlibrary

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.lang.ref.WeakReference
import java.util.ArrayList

@SuppressLint("MissingPermission")
class AirLocation(
    activity: Activity,
    private val shouldWeRequestPermissions: Boolean,
    private val shouldWeRequestOptimization: Boolean,
    private val callbacks: Callbacks
) {
    private var activityWeakReference = WeakReference<Activity>(activity)
    private var locationCallback: LocationCallback? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val requestCheckSettings = 1235
    private val requestLocation = 1236

    interface Callbacks {
        fun onSuccess(location: Location)
        fun onFailed(locationFailedEnum: LocationFailedEnum)
    }

    enum class LocationFailedEnum {
        DeviceInFlightMode,
        LocationPermissionNotGranted,
        LocationOptimizationPermissionNotGranted,
        HighPrecisionNA_TryAgainPreferablyWithInternet
    }

    init {
        // First try to get last available location, if it matches our precision level
        fusedLocationClient = activity.let { LocationServices.getFusedLocationProviderClient(it) }
        val task = fusedLocationClient?.lastLocation

        task?.addOnSuccessListener { location: Location? ->
            if (location != null) {
                callbacks.onSuccess(location)
            } else {
                onLastLocationFailed()
            }
        }
        task?.addOnFailureListener {
            onLastLocationFailed()
        }
    }

    private fun onLastLocationFailed() {
        // define location callback now
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                callbacks.onSuccess(locationResult.lastLocation)
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }

            @SuppressLint("MissingPermission")
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                if (locationAvailability?.isLocationAvailable == false) {
                    callbacks.onFailed(LocationFailedEnum.HighPrecisionNA_TryAgainPreferablyWithInternet)
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                }
            }
        }

        // check flight mode
        if (activityWeakReference.get() == null) {
            return
        }

        if (NetworkUtil.isInFlightMode(activityWeakReference.get() as Activity)) {
            callbacks.onFailed(LocationFailedEnum.DeviceInFlightMode)
        } else {
            // check location permissions
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            var permissionGranted = true
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(activityWeakReference.get() as Activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false
                    break
                }
            }
            if (permissionGranted == false) {
                // request permissions as not present
                if (shouldWeRequestPermissions) {
                    val permissionsArgs = permissions.toTypedArray()
                    ActivityCompat.requestPermissions(activityWeakReference.get() as Activity, permissionsArgs, requestLocation)
                } else {
                    callbacks.onFailed(LocationFailedEnum.LocationPermissionNotGranted)
                }
            } else {
                getLocation()
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (activityWeakReference.get() == null) {
            return
        }

        if (requestCode == requestLocation) {
            if (grantResults.isEmpty()) {
                callbacks.onFailed(LocationFailedEnum.LocationPermissionNotGranted)
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
                getLocation()
            } else {
                callbacks.onFailed(LocationFailedEnum.LocationPermissionNotGranted)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        if (activityWeakReference.get() == null) {
            return
        }

        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        // check current location settings
        val task: Task<LocationSettingsResponse> = (LocationServices.getSettingsClient(activityWeakReference.get() as Activity))
            .checkLocationSettings((LocationSettingsRequest.Builder().addLocationRequest(locationRequest)).build())

        task.addOnSuccessListener { locationSettingsResponse ->
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                if (activityWeakReference.get() == null) {
                    return@addOnFailureListener
                }

                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    if (shouldWeRequestOptimization) {
                        exception.startResolutionForResult(activityWeakReference.get() as Activity, requestCheckSettings)
                    } else {
                        callbacks.onFailed(LocationFailedEnum.LocationOptimizationPermissionNotGranted)
                    }
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (activityWeakReference.get() == null) {
            return
        }

        if (requestCode == requestCheckSettings) {
            if (resultCode == Activity.RESULT_OK) {
                getLocation()
            } else {
                val locationManager = (activityWeakReference.get() as Activity).getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    callbacks.onFailed(LocationFailedEnum.HighPrecisionNA_TryAgainPreferablyWithInternet)
                } else {
                    callbacks.onFailed(LocationFailedEnum.LocationOptimizationPermissionNotGranted)
                }
            }
        }
    }

}