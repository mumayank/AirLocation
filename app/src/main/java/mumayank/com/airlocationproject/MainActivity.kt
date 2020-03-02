package mumayank.com.airlocationproject

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import mumayank.com.airlocationlibrary.AirLocation

class MainActivity : AppCompatActivity() {

    // Declare your airLocation object on top
    private var airLocation: AirLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        progressBar.visibility = View.GONE

        button.setOnClickListener {

            // Tip: As fetching location is a time consuming process,
            // before initializing airLocation object, show a progressBar maybe
            progressBar.visibility = View.VISIBLE
            button.visibility = View.INVISIBLE

            airLocation = AirLocation(this, true, true, object : AirLocation.Callbacks {
                override fun onSuccess(location: Location) {
                    progressBar.visibility = View.GONE
                    button.visibility = View.VISIBLE
                    val string =
                        "LONG=${location.longitude}\nLAT=${location.latitude}\n\n${textView.text}"
                    textView.text = string
                    Log.d("airlocationlog", string)
                }

                override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    progressBar.visibility = View.GONE
                    button.visibility = View.VISIBLE
                    // either do nothing, or show error which is received as locationFailedEnum
                    Toast.makeText(this@MainActivity, locationFailedEnum.name, Toast.LENGTH_SHORT)
                        .show()
                    Log.d("airlocationlog", locationFailedEnum.name)
                }

            })

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        airLocation?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}