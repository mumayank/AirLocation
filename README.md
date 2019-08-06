![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/image.png "Logo")

# AirLocation
[![](https://jitpack.io/v/mumayank/AirLocation.svg)](https://jitpack.io/#mumayank/AirLocation)
<span class="badge-paypal"><a href="https://www.paypal.me/mumayank" title="Donate to this project using Paypal"><img src="https://img.shields.io/badge/paypal-donate-yellow.svg" alt="PayPal donate button" /></a></span>

An Android library which simply gets you user's most precise current location via a callback!
+ Location is precise up to 7 decimal places (highest precision)
+ No need to add any permissions in manifest manually
+ No need to add google play services location lib in gradle manually
+ Uses Google location services API internally - so you're in safe hands
+ Simple plug and play design
+ **Full Java support**

###### (method counts ~50, size ~50KB)

# Screenshots

|   |  |
| ------------- | ------------- |
| ![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/s1.png "Logo")  | ![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/s2.png "Logo")  |
| ![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/s3.png "Logo")  | ![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/s4.png "Logo")  |
| ![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/s5.png "Logo")  | ![alt text](https://github.com/mumayank/AirLocation/blob/master/github_assets/s6.png "Logo")  |

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

# Java Support
This library fully supports Java out of the box (because Kotlin is 100% interoperable)
Hence the setup remains the same, and usage becomes:
```java
public class MainJavaActivity extends AppCompatActivity {

    // In your activity, define this variable at top-level
    private AirLocation airLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_java);

        // Fetch location simply like this whenever you need
        airLocation = new AirLocation(this, true, true, new AirLocation.Callbacks() {
            @Override
            public void onSuccess(@NotNull Location location) {
                // do something
            }

            @Override
            public void onFailed(@NotNull AirLocation.LocationFailedEnum locationFailedEnum) {
                // do something
            }
        });
    }

    // override and call airLocation object's method by the same name
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        airLocation.onActivityResult(requestCode, resultCode, data);
    }

    // override and call airLocation object's method by the same name
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
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

I am working on getting live location of user. Will update the library soon. Stay tuned!

Thank you :)
