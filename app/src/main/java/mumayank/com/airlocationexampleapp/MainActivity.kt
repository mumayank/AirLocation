package mumayank.com.airlocationexampleapp

import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.main_activity.*
import mumayank.com.airlocationlibrary.AirLocation
import mumayank.com.airlocationlibrary.AirLocationActivity

class MainActivity : AirLocationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        progressBar.visibility = View.GONE

        button.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            button.visibility = View.INVISIBLE

            airLocation = AirLocation(this, true, true, object: AirLocation.LocationUtilCallbacks{
                override fun onSuccess(location: Location) {
                    textView.text = "LONG=${location.longitude}\nLAT=${location.latitude}\n\n${textView.text}"
                    progressBar.visibility = View.GONE
                    button.visibility = View.VISIBLE
                }

                override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    progressBar.visibility = View.GONE
                    button.visibility = View.VISIBLE
                }
            })
        }


    }
}
