package mumayank.com.airlocationproject

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import mumayank.com.airlocationlibrary.AirLocation
import java.util.*

class MainActivity : AppCompatActivity() {

    // Declare your airLocation object on top
    private var airLocation1: AirLocation? = null
    private var airLocation2: AirLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        progressBar.visibility = View.GONE

        button1.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            airLocation1 = AirLocation(this, object : AirLocation.Callback {
                override fun aOnSuccess(locations: ArrayList<Location>) {
                    progressBar.visibility = View.GONE
                    val string =
                        "${locations[0].longitude}, ${locations[0].latitude}\n\n${textView1.text}"
                    textView1.text = string
                }

                override fun bOnFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, locationFailedEnum.name, Toast.LENGTH_SHORT)
                        .show()
                }
            }, true)
        }

        button2.setOnClickListener {
            progressBar.visibility = View.VISIBLE

            airLocation2 = AirLocation(this, object : AirLocation.Callback {
                override fun aOnSuccess(locations: ArrayList<Location>) {
                    progressBar.visibility = View.GONE
                    var string = "\n"
                    for (location in locations) {
                        string = "${location.longitude}, ${location.latitude}\n$string"
                    }
                    string = "$string${textView2.text}"
                    textView2.text = string
                }

                override fun bOnFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, locationFailedEnum.name, Toast.LENGTH_SHORT)
                        .show()
                }
            })

        }

        button3.setOnClickListener {
            airLocation2?.stopLocationUpdates()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        airLocation1?.onActivityResult(requestCode, resultCode, data)
        airLocation2?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        airLocation1?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airLocation2?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}