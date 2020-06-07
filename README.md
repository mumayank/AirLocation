


![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/image.png "Logo")

# AirLocation
[![](https://jitpack.io/v/mumayank/AirLocation.svg)](https://jitpack.io/#mumayank/AirLocation)
<span class="badge-paypal"><a href="https://www.paypal.me/mumayank" title="Donate to this project using Paypal"><img src="https://img.shields.io/badge/paypal-donate-yellow.svg" alt="PayPal donate button" /></a></span>
<a href="http://developer.android.com/index.html" target="_blank"><img src="https://img.shields.io/badge/platform-android-green.svg"/></a> <a href="https://android-arsenal.com/api?level=17" target="_blank"><img src="https://img.shields.io/badge/API-17%2B-green.svg?style=flat"/></a> 

An android library to simplify the usage of Google Play services location APIs, to get user's most precise live location via a callback!

Jump to [Setup](https://github.com/mumayank/AirLocation/blob/master/README.md#setup "Setup") or [Usage](https://github.com/mumayank/AirLocation/blob/master/README.md#usage "Usage")

Features:
+ The location is precise up to 7 decimal places
+ Choose to get user's location just one-time, or continue getting live udpates
+ The library takes care of a host of redundant checks and tasks like:
	+ Declaring the location permissions in the Manifest file
	+ Adding the Google Play services location APIs library dependency in Gradle
	+ Checking if Google Play services are available and up to date or not.
		+ If not, requesting the user to update it, along with providing an option to do so.
	+ Checking if location permissions are available or not. 
		+ If not, requesting the user at runtime to grant the permissions. 
		+ Also checking if the permissions are permanently disabled by the user.
			+ If so, taking the user to the app's settings page and requesting to manually grant the permissions.
	+ Checking if the device hardware settings are optimized or not (GPS is on, Wifi is on, etc)
		+ If not, requesting the user to grant permission to change settings.
+ Uses only Google Play services location APIs internally - so you're in safe hands
+ Simple plug and play design
+ Extremely light weight library (~50KB)
+ **Written in Kotlin (with full Java support)**
+ Android 10+ compatible (gets user's location via foreground location access, i.e., the activity requesting the location must be visible to the user to continue receiving location updates)
+ Takes care of the activity lifecycle

# Screenshots

|   |  |
| ------------- | ------------- |
| <img src="https://github.com/mumayank/AirLocation/blob/master/github_assets/s1.png" width="300">  | <img src="https://github.com/mumayank/AirLocation/blob/master/github_assets/s2.png" width="300">  |
| <img src="https://github.com/mumayank/AirLocation/blob/master/github_assets/s3.png" width="300">    | 

# Setup

Add this line in your root build.gradle at the end of repositories:

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' } // this line
  }
}
  ```
Add this line in your app build.gradle:
```gradle
dependencies {
  implementation 'com.github.mumayank:AirLocation:LATEST_VERSION' // this line
}
```
where LATEST_VERSION is [![](https://jitpack.io/v/mumayank/AirLocation.svg)](https://jitpack.io/#mumayank/AirLocation)

# Usage

1. Define `airLocation`
2. To start receiving live location, call `airLocation.start()`
3. Override `onActivityResult` and call `airLocation.onActivityResult()`
4. Override `onRequestPermissionsResult` and call `airLocation.onRequestPermissionsResult()`
5. (Optional) To stop receiving the live location, call `airLocation.stopLocationUpdates()`

Example:

```kotlin
class MainActivity : AppCompatActivity() {

    private val airLocation = AirLocation(this, object : AirLocation.Callback {  
	    override fun onSuccess(locations: ArrayList<Location>) {  
	        // do something 
	        // the entire track is sent in locations
	    }  
	  
	    override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {  
	        // do something 
	        // the reason for failure is given in locationFailedEnum
	    }  
	})

	override fun onCreate(savedInstanceState: Bundle?) {  
	    ...
	    airLocation.start() // CALL .start() WHEN YOU ARE READY TO RECEIVE LOCATION UPDATES
    }
    
    ...
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data) // ADD THIS LINE INSIDE onActivityResult
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        airLocation?.onRequestPermissionsResult(requestCode, permissions, grantResults) // ADD THIS LINE INSIDE onRequestPermissionResult
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    
}
```

+ In some cases, you'd want to just get user's live location once. In such a case, simply pass `true` as the value of the parameter `isLocationRequiredOnlyOneTime`:
```kotlin
private val airLocation = AirLocation(this, object : AirLocation.Callback {  
    override fun onSuccess(locations: ArrayList<Location>) {  }  
    override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {  }  
}, true) // NOTE HERE: PASS true TO JUST GET USER'S LIVE LOCATION ONCE
```

## Thank you!
If you love the library, or it has helped you in any way, please give it a star! I'd really appreciate it!

Post your feature requests/ pull requests/ bug reports in the appropriate section of this repositories.

Thank you :)
