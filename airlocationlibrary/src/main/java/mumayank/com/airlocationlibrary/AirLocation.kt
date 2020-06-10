package mumayank.com.airlocationlibrary

import android.Manifest
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
import mumayank.com.airpermissions.AirPermissions
import java.io.Serializable
import java.lang.ref.WeakReference

@SuppressLint("MissingPermission")
class AirLocation(
    activity: Activity,
    private val callback: Callback?,
    private val isLocationRequiredOnlyOneTime: Boolean = false,
    private val locationInterval: Long = 0,
    private val toastTextWhenOpenAppSettingsIfPermissionsPermanentlyDenied: String = "Please enable location permissions from settings to proceed"
): Serializable {
    /*
    declarations
     */
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isStartCalled = false

    private val activityWeakReference = WeakReference(activity)

    private val googlePlayApiHelper = GooglePlayApiHelper(activity, fun() {
        if (activityWeakReference.get() == null) {
            return
        }

        getLocationPermissions()
    }, fun() {
        if (activityWeakReference.get() == null) {
            return
        }

        callback?.onFailure(LocationFailedEnum.GOOGLE_PLAY_API_NOT_AVAILABLE)
    })

    private val airPermissions = AirPermissions(activity, arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ), fun() {
        checkIfInFlightMode()
    }, fun() {
        callback?.onFailure(LocationFailedEnum.LOCATION_PERMISSION_NOT_GRANTED)
    })

    private val locationOptimizationPermissionHelper = LocationOptimizationPermissionHelper(
        activity,
        locationInterval,
        isLocationRequiredOnlyOneTime,
        fun() {
            if (activityWeakReference.get() == null) {
                return
            }

            getFusedLocation()
        }, fun(locationFailedEnum: LocationFailedEnum) {
            if (activityWeakReference.get() == null) {
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
        if (activityWeakReference.get() == null) {
            return
        }

        isStartCalled = true
        makeGooglePlayApiAvailable()
    }

    private fun makeGooglePlayApiAvailable() {
        if (activityWeakReference.get() == null) {
            return
        }

        googlePlayApiHelper.makeItAvailable()
    }

    private fun getLocationPermissions() {
        if (activityWeakReference.get() == null) {
            return
        }

        airPermissions.request()
    }

    private fun checkIfInFlightMode() {
        if (activityWeakReference.get() == null) {
            return
        }

        if (NetworkHelper.isInFlightMode(activityWeakReference.get() as Activity)) {
            callback?.onFailure(LocationFailedEnum.DEVICE_IN_FLIGHT_MODE)
        } else {
            getOptimizationPermissions()
        }
    }

    private fun getOptimizationPermissions() {
        if (activityWeakReference.get() == null) {
            return
        }

        locationOptimizationPermissionHelper.getPermission()
    }

    private fun getFusedLocation() {
        val activityTemp = activityWeakReference.get() ?: return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activityTemp)
        val task = fusedLocationClient.lastLocation
        task?.addOnSuccessListener { location: Location? ->
            if (activityWeakReference.get() == null) {
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
            if (activityWeakReference.get() == null) {
                return@addOnFailureListener
            }

            addLifecycleListener()
        }
    }

    private fun addLifecycleListener() {
        val activityTemp = activityWeakReference.get() ?: return

        (activityTemp as LifecycleOwner).lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun connectListener() {
                if (activityWeakReference.get() == null) {
                    return
                }

                requestLocationUpdates()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun disconnectListener() {
                if (activityWeakReference.get() == null) {
                    return
                }

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        })
    }

    private fun requestLocationUpdates() {
        if (activityWeakReference.get() == null) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (activityWeakReference.get() == null) {
                    return
                }

                locationResult ?: return
                callback?.onSuccess(locationResult.locations as ArrayList<Location>)
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                if (activityWeakReference.get() == null) {
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
        if (activityWeakReference.get() == null) {
            return
        }

        if (isStartCalled.not()) {
            return
        }

        airPermissions.onRequestPermissionsResult(requestCode)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (activityWeakReference.get() == null) {
            return
        }

        if (isStartCalled.not()) {
            return
        }

        airPermissions.onActivityResult(requestCode)
        locationOptimizationPermissionHelper.onActivityResult(requestCode, resultCode, data)
        googlePlayApiHelper.onActivityResult(requestCode)
    }

}