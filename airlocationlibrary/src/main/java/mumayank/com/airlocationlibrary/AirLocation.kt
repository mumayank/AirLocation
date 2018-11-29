package mumayank.com.airlocationlibrary


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import mumayank.com.airdialog.AirDialog
import java.util.ArrayList

@SuppressLint("MissingPermission")
class AirLocation(
    private val airLocationActivity: AirLocationActivity,
    private val shouldWeRequestPermissions: Boolean,
    private val shouldWeRequestOptimization: Boolean,
    private val callbacks: Callbacks
) {

    /**
     * TODO USAGES:
     * 1. Extend AirLocationActivity
     * 2. If Override onActivityResult and onRequestPermissionsResult, call super first
     * 4. For every location request, init airLocation var, already available from AirLocationActivity (your activity's parent)
     */

    interface Callbacks {
        fun beforeStart()
        fun onComplete()
        fun onSuccess(location: Location)
        fun onFailed(locationFailedEnum: LocationFailedEnum)
    }

    var locationCallback: LocationCallback? = null
    var fusedLocationClient: FusedLocationProviderClient? = null

    enum class LocationFailedEnum(val message: String, val resId: Int) {
        DeviceInFlightMode("Turn off the flight mode and try again.", R.drawable.ic_airplanemode_inactive_black_24dp),
        NoPermissions("Provide location permission and try again.", R.drawable.ic_location_on_black_24dp),
        NoOptimizations("Provide location optimization permission and try again.", R.drawable.ic_my_location_black_24dp),
        NoHighAccuracy("Signal is weak. Try again in some time.\n\nTip: Connect to internet for increased accuracy.", R.drawable.ic_signal_cellular_connected_no_internet_0_bar_black_24dp)
    }

    val REQUEST_CHECK_SETTINGS = 1234
    val REQUEST_LOCATION = 1235

    init {
        callbacks.beforeStart()

        fusedLocationClient = airLocationActivity?.let { LocationServices.getFusedLocationProviderClient(it) }
        val task = fusedLocationClient?.lastLocation

        task?.addOnSuccessListener { location: Location? ->
            if (location != null) {
                callbacks.onComplete()
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
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                callbacks.onComplete()
                callbacks.onSuccess(locationResult.lastLocation)
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }

            @SuppressLint("MissingPermission")
            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                super.onLocationAvailability(locationAvailability)
                if (locationAvailability?.isLocationAvailable == false) {
                    showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.NoHighAccuracy)
                    fusedLocationClient?.removeLocationUpdates(locationCallback)
                }
            }
        }

        // check flight mode
        if (NetworkUtil.isInFlightMode(airLocationActivity as AirLocationActivity)) {
            showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.DeviceInFlightMode)
        } else {
            // check location permissions
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            var permissionGranted = true
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(airLocationActivity as AirLocationActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false
                    break
                }
            }
            if (permissionGranted == false) {
                // request permissions as not present
                if (shouldWeRequestPermissions) {
                    val permissionsArgs = permissions.toTypedArray()
                    ActivityCompat.requestPermissions(airLocationActivity as AirLocationActivity, permissionsArgs, REQUEST_LOCATION)
                } else {
                    showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.NoPermissions)
                }
            } else {
                getLocation()
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grandResults: ArrayList<Int>) {

        if (airLocationActivity == null) {
            return
        }

        if (requestCode == REQUEST_LOCATION) {
            if (grandResults.isEmpty()) {
                showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.NoPermissions)
                return
            }

            var granted = true
            for (grantResult in grandResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false
                    break
                }
            }
            if (granted) {
                getLocation()
            } else {
                showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.NoPermissions)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        val locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        // check current location settings
        val task: Task<LocationSettingsResponse> = (LocationServices.getSettingsClient(airLocationActivity as AirLocationActivity))
            .checkLocationSettings((LocationSettingsRequest.Builder().addLocationRequest(locationRequest)).build())

        task.addOnSuccessListener { locationSettingsResponse ->
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    if (shouldWeRequestOptimization) {
                        exception.startResolutionForResult(airLocationActivity, REQUEST_CHECK_SETTINGS)
                    } else {
                        showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.NoOptimizations)
                    }
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (airLocationActivity == null) {
            return
        }

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                getLocation()
            } else {
                val locationManager = airLocationActivity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.NoHighAccuracy)
                } else {
                    showErrorDialog(airLocationActivity as AirLocationActivity, callbacks, LocationFailedEnum.NoOptimizations)
                }
            }
        }
    }

    companion object {

        fun showErrorDialog(
            activity: Activity,
            callbacks: Callbacks,
            locationFailedEnum: LocationFailedEnum
        ) {
            AirDialog.show(
                activity,
                "Unable to fetch location",
                locationFailedEnum.message,
                locationFailedEnum.resId,
                false,
                AirDialog.Button("OK") {
                    callbacks.onComplete()
                    callbacks.onFailed(locationFailedEnum)
                }
            )
        }

    }

}