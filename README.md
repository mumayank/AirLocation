# AirLocation
An Android library which simply gets you user's most accurate current location. Period.
+ Location is accurate up to 7 decimal places (highest accuracy)
+ No need to add any permissions in manifest
+ No need to add google play services location lib in gradle
+ No need to update this library from time to time (latest update are automatically fetched on the fly!)
+ Simple plug and play design.

# Setup

#### Setup #1 - Project's build.gradle
In your project's `build.gradle` file, under `allprojects`, under `repositories`, add the following line:

```gradle
maven { url 'https://jitpack.io' }
```

#### Setup #2 - App's build.gradle
In your app's `build.gradle` file, under `dependencies`, add the following line:

```gradle
implementation 'com.github.mumayank:AirLocation:master-SNAPSHOT'
```

# Usage

#### Rule #1

The activity in which you want to fetch location, must extend `AirLocationActivity` 

(FYI: AirLocationActivity extends AppCompatActivity)

#### Rule #2

Everytime you want to fetch location, just initialize `airLocation` variable (available from parent class `AirLocationActivity`)

Example:

```kotlin
airLocation = AirLocation(this, true, true, object: AirLocation.LocationUtilCallbacks {
                override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    // do something
                }

                override fun onSuccess(location: Location) {
                    // do something
                }
            })
```

#### Rule #3 - IMP

If you ever `override` `onActivityResult` or `onRequestPermissionsResult` functions in your activity, then you must call their respective super functions.
