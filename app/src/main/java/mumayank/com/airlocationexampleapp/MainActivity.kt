package mumayank.com.airlocationexampleapp

import android.location.Location
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.main_activity.*
import mumayank.com.airlocationlibrary.AirLocation
import mumayank.com.airlocationlibrary.AirLocationActivity

class MainActivity : AirLocationActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        progressBar.visibility = View.GONE

        button.setOnClickListener {

            airLocation = AirLocation(this, true, true, object: AirLocation.Callbacks {
                override fun beforeStart() {
                    progressBar.visibility = View.VISIBLE
                    button.visibility = View.INVISIBLE
                }

                override fun onComplete() {
                    progressBar.visibility = View.GONE
                    button.visibility = View.VISIBLE
                }

                override fun onSuccess(location: Location) {
                    textView.text = "LONG=${location.longitude}\nLAT=${location.latitude}\n\n${textView.text}"
                }

                override fun onFailed(locationFailedEnum: AirLocation.LocationFailedEnum) {
                    // do nothing
                }

            })

        }

    }
}