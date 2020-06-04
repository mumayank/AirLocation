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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import java.lang.ref.WeakReference
import java.util.*

@SuppressLint("MissingPermission")
class AirLocation(
    private val activity: Activity?,
    private val callback: Callback?,
    private val isLocationRequiredOnlyOneTime: Boolean = false,
    private val locationInterval: Long = 1000
) {
    private var activityWeakReference = WeakReference<Activity>(activity)
    private var locationCallback: LocationCallback? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val requestCheckSettings = 1235
    private val requestLocation = 1236
    private var isStopLocationUpdateRequested = false

    enum class LocationFailedEnum {
        DeviceInFlightMode,
        LocationPermissionNotGranted,
        LocationOptimizationPermissionNotGranted,
        HighPrecisionNaTryAgainPreferablyWithInternet
    }

    interface Callback {
        fun aOnSuccess(locations: ArrayList<Location>)
        fun bOnFailed(locationFailedEnum: LocationFailedEnum)
    }

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        val task = fusedLocationClient?.lastLocation

        task?.addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback?.aOnSuccess(arrayListOf(location))
                if (isLocationRequiredOnlyOneTime.not()) {
                    addLifecycleListener()
                }
            } else {
                addLifecycleListener()
            }
        }?.addOnFailureListener {
            addLifecycleListener()
        }
    }

    private fun addLifecycleListener() {
        (activity as LifecycleOwner).lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun connectListener() {
                if (isStopLocationUpdateRequested.not()) {
                    addLocationCallback()
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun disconnectListener() {
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }
        })
    }

    private fun addLocationCallback() {

        // define location callback now
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                callback?.aOnSuccess(locationResult.locations as ArrayList<Location>)
            }

            @SuppressLint("MissingPermission")
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                if (locationAvailability?.isLocationAvailable == false) {
                    callback?.bOnFailed(LocationFailedEnum.HighPrecisionNaTryAgainPreferablyWithInternet)
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                }
            }
        }

        // check flight mode
        if (activityWeakReference.get() == null) {
            return
        }

        if (NetworkUtil.isInFlightMode(activityWeakReference.get() as Activity)) {
            callback?.bOnFailed(LocationFailedEnum.DeviceInFlightMode)
        } else {
            // check location permissions
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            var permissionGranted = true
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        activityWeakReference.get() as Activity,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionGranted = false
                    break
                }
            }
            if (permissionGranted == false) {
                // request permissions as not present
                val permissionsArgs = permissions.toTypedArray()
                ActivityCompat.requestPermissions(
                    activityWeakReference.get() as Activity,
                    permissionsArgs,
                    requestLocation
                )
            } else {
                getLocation()
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        if (activityWeakReference.get() == null) {
            return
        }

        val locationRequest = LocationRequest().apply {
            interval = locationInterval
            fastestInterval = locationInterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            if (isLocationRequiredOnlyOneTime) numUpdates = 1
        }

        // check current location settings
        val task: Task<LocationSettingsResponse> =
            (LocationServices.getSettingsClient(activityWeakReference.get() as Activity))
                .checkLocationSettings(
                    (LocationSettingsRequest.Builder().addLocationRequest(
                        locationRequest
                    )).build()
                )

        task.addOnSuccessListener { locationSettingsResponse ->
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                if (activityWeakReference.get() == null) {
                    return@addOnFailureListener
                }

                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        activityWeakReference.get() as Activity,
                        requestCheckSettings
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (activityWeakReference.get() == null) {
            return
        }

        if (requestCode == requestLocation) {
            if (grantResults.isEmpty()) {
                callback?.bOnFailed(LocationFailedEnum.LocationPermissionNotGranted)
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
                callback?.bOnFailed(LocationFailedEnum.LocationPermissionNotGranted)
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
                val locationManager =
                    (activityWeakReference.get() as Activity).getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    callback?.bOnFailed(LocationFailedEnum.HighPrecisionNaTryAgainPreferablyWithInternet)
                } else {
                    callback?.bOnFailed(LocationFailedEnum.LocationOptimizationPermissionNotGranted)
                }
            }
        }
    }

    fun stopLocationUpdates() {
        isStopLocationUpdateRequested = true
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

}