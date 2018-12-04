![alt text](https://github.com/mumayank/AirLocation/blob/master/image.png "Logo")

# AirLocation

An Android library which simply gets you user's most precise current location via a callback!
+ Location is precise up to 7 decimal places (highest precision)
+ No need to add any permissions in manifest manually
+ No need to add google play services location lib in gradle manually
+ Uses Google location services API internally - so you're in safe hands
+ Simple plug and play design

###### (method counts ~50, size ~50KB)

[![](https://jitpack.io/v/mumayank/AirLocation.svg)](https://jitpack.io/#mumayank/AirLocation)

# Screenshots

|   |  |
| ------------- | ------------- |
| ![alt text](https://github.com/mumayank/AirLocation/blob/master/s1.png "Logo")  | ![alt text](https://github.com/mumayank/AirLocation/blob/master/s2.png "Logo")  |
| ![alt text](https://github.com/mumayank/AirLocation/blob/master/s3.png "Logo")  | ![alt text](https://github.com/mumayank/AirLocation/blob/master/s4.png "Logo")  |
| ![alt text](https://github.com/mumayank/AirLocation/blob/master/s5.png "Logo")  | ![alt text](https://github.com/mumayank/AirLocation/blob/master/s6.png "Logo")  |

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

+ Everytime you want to fetch user's current location, simply initialize `airLocation` variable:
```kotlin
airLocation = AirLocation(this, true, true, object: AirLocation.Callbacks {
                override fun onSuccess(location: Location) {
                    // location fetched successfully, proceed with it
                }

                override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    // couldn't fetch location due to reason available in locationFailedEnum
                    // you may optionally do something to inform the user, even though the reason may be obvious
                }

            })
```

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

