

![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/image.png "Logo")

# AirLocation
[![](https://jitpack.io/v/mumayank/AirLocation.svg)](https://jitpack.io/#mumayank/AirLocation)
<span class="badge-paypal"><a href="https://www.paypal.me/mumayank" title="Donate to this project using Paypal"><img src="https://img.shields.io/badge/paypal-donate-yellow.svg" alt="PayPal donate button" /></a></span>

Android library to get user's most precise live location and its updates via a callback!

Jump to [Setup](https://github.com/mumayank/AirLocation/blob/master/README.md#setup "Setup") or [Usage](https://github.com/mumayank/AirLocation/blob/master/README.md#usage "Usage")

Features:
+ Highest precision: The location is precise up to 7 decimal places
+ Either get user's location just one-time, or continue getting live udpates
+ No need to manually:
    + add any permissions in manifest
    + add google play services location lib in gradle
    + ask runtime permissions
    + ask location settings optimization permissions
+ Uses Google location services API internally - so you're in safe hands
+ Simple plug and play design
+ Extremely light weight library (~50KB)
+ **Written in Kotlin (with full Java support)**
+ Android 10+ compatible (gets user's location via foreground location access, i.e., an activity that belongs to your app must be visible to the user to continue receiving location updates)
+ **Import the lib, use the lib, and start receiving user's live location in under 5 mins!**

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

+ Declare airLocation in your activity
+ Override `onActivityResult` and call `airLocation.onActivityResult` inside it
+ Override `onRequestPermissionsResult` and call `airLocation.onRequestPermissionsResult` inside it

Example:

```kotlin
class MainActivity : AppCompatActivity() {

    private var airLocation: AirLocation? = null // ADD THIS LINE ON TOP
    
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

+ When you  want to start receiving user's live location udpates, simply initialize `airlocation` variable:
```kotlin
airLocation = AirLocation(activity, object: AirLocation.Callback {  
      
    override fun aOnSuccess(locations: ArrayList<Location>) {  
        // using the given locations array list, you can easily 
        // trace the user path of the live location  
    }  
  
    override fun bOnFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {  
        // couldn't fetch location due to reason available in locationFailedEnum
    }  
  
})
```

+ In some cases, you'd want to just get user's live location once. In such a case, simply pass `true` as the value of the parameter `isLocationRequiredOnlyOneTime`:
```kotlin
airLocation = AirLocation(activity, object: AirLocation.Callback {  
  
    override fun aOnSuccess(locations: ArrayList<Location>) {  
         
    }  
  
    override fun bOnFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {  
        
    }  
  
}, true) // NOTE HERE: PASS TRUE TO JUST GET USER'S LIVE LOCATION ONCE
```

# How it works?

I was working on an app where I was required to fetch my app user's current location with highest precision, and accuracy.
I looked it up on Android app development documentation where it was mentioned to use Google location services library for the same. My experience wasn't pleasant. It is difficult, complex, and painful to implement. I mean, everytime I want to fetch user's location, I have to do all those things! No way!

I started looking for alternatives. I mean, why do so much of work for such a redundant task? But I couldn't find a suitable library for my use. Hence, I decided to go back to Google location services library. After days of struggle, I was finally able to make it work for all scenarios. Then it hit me - so many developers out there might be going through the same pain as I, specially junior android app developers. Shouldn't there be something as simple as a callback to get user's current location? Hence I decided to publish my work in the form of this library.

+ You don't have to manually add permissions in manifest as the library's manifest contains those and whenever your app builds, manifests are merged resulting in permissions getting added to your app's manifest automatically.
+ You don't have to add Google location services library in your app's gradle.build file as the library's gradle.build file contains it
+ You don't have to ask location permission or location optimization permission at runtime because the library handles that for you
+ You will simply get the location via plug and play of this library.

# Future

The library is in active development. Please post your feature requests/ pull requests/ bug reports in the appropriate section of this repositories.

Thank you :)
