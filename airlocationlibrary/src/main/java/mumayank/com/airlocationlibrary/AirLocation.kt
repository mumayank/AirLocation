package mumayank.com.airlocationlibrary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.gms.location.*
import mumayank.com.airlocationlibrary.helpers.*
import java.io.Serializable
import java.lang.ref.WeakReference

@SuppressLint("MissingPermission")
class AirLocation(
    private val activity: Activity,
    private val callback: Callback?,
    private val isLocationRequiredOnlyOneTime: Boolean = false,
    private val locationInterval: Long = 1000,
    private val toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied: String = "Please enable location permissions from settings to proceed"
): Serializable {
    /*
    declarations
     */
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isStartCalled = false
    private var isStopLocationUpdateRequested = false

    private val activityWeakReference = WeakReference(activity)

    private val googlePlayApiHelper = GooglePlayApiHelper(activity, activityWeakReference, fun() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        getLocationPermissions()
    }, fun() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        callback?.onFailure(LocationFailedEnum.GOOGLE_PLAY_API_NOT_AVAILABLE)
    })

    private val locationPermissionHelper = LocationPermissionHelper(activity, activityWeakReference, fun() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        checkIfInFlightMode()
    }, fun() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        callback?.onFailure(LocationFailedEnum.LOCATION_PERMISSION_NOT_GRANTED)
    }, toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied)

    private val locationOptimizationPermissionHelper = LocationOptimizationPermissionHelper(
        activity,
        activityWeakReference,
        locationInterval,
        isLocationRequiredOnlyOneTime,
        fun() {
            if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                return
            }

            getFusedLocation()
        }, fun(locationFailedEnum: LocationFailedEnum) {
            if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                return
            }

            callback?.onFailure(locationFailedEnum)
        }
    )

    enum class LocationFailedEnum {
        GOOGLE_PLAY_API_NOT_AVAILABLE,
        DEVICE_IN_FLIGHT_MODE,
        LOCATION_PERMISSION_NOT_GRANTED,
        LOCATION_OPTIMIZATION_PERMISSION_NOT_GRANTED,
        COULD_NOT_OPTIMIZE_DEVICE_HARDWARE,
        HIGH_PRECISION_LOCATION_NA_TRY_AGAIN_PREFERABLY_WITH_NETWORK_CONNECTIVITY
    }

    interface Callback {
        fun onSuccess(locations: ArrayList<Location>)
        fun onFailure(locationFailedEnum: LocationFailedEnum)
    }

    /*
    start of logic
     */
    fun start() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        isStartCalled = true
        isStopLocationUpdateRequested = false
        makeGooglePlayApiAvailable()
    }

    private fun makeGooglePlayApiAvailable() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        googlePlayApiHelper.makeItAvailable()
    }

    private fun getLocationPermissions() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        locationPermissionHelper.getPermissions()
    }

    private fun checkIfInFlightMode() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        if (NetworkHelper.isInFlightMode(activityWeakReference.get() as Activity)) {
            callback?.onFailure(LocationFailedEnum.DEVICE_IN_FLIGHT_MODE)
        } else {
            getOptimizationPermissions()
        }
    }

    private fun getOptimizationPermissions() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        locationOptimizationPermissionHelper.getPermission()
    }

    private fun getFusedLocation() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        val task = fusedLocationClient.lastLocation
        task?.addOnSuccessListener { location: Location? ->
            if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                return@addOnSuccessListener
            }

            if (location != null) {
                callback?.onSuccess(arrayListOf(location))
                if (isLocationRequiredOnlyOneTime.not()) {
                    addLifecycleListener()
                }
            } else {
                addLifecycleListener()
            }
        }?.addOnFailureListener {
            if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                return@addOnFailureListener
            }

            addLifecycleListener()
        }
    }

    private fun addLifecycleListener() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        (activity as LifecycleOwner).lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun connectListener() {
                if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                    return
                }

                if (isStopLocationUpdateRequested.not()) {
                    requestLocationUpdates()
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun disconnectListener() {
                if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                    return
                }

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        })
    }

    private fun requestLocationUpdates() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                    return
                }

                locationResult ?: return
                callback?.onSuccess(locationResult.locations as ArrayList<Location>)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
                    return
                }

                if (locationAvailability?.isLocationAvailable == false) {
                    callback?.onFailure(LocationFailedEnum.HIGH_PRECISION_LOCATION_NA_TRY_AGAIN_PREFERABLY_WITH_NETWORK_CONNECTIVITY)
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            LocationOptimizationPermissionHelper.getLocationRequest(
                locationInterval,
                isLocationRequiredOnlyOneTime
            ),
            locationCallback,
            Looper.myLooper()
        )
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        if (isStartCalled.not()) {
            return
        }

        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        if (isStartCalled.not()) {
            return
        }

        locationOptimizationPermissionHelper.onActivityResult(requestCode, resultCode, data)
        googlePlayApiHelper.onActivityResult(requestCode, resultCode, data)
    }

    fun stopLocationUpdates() {
        if (ActivityHelper.isActivityWeakReferenceNull(activityWeakReference)) {
            return
        }

        if (isStartCalled.not()) {
            return
        }

        isStopLocationUpdateRequested = true
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}