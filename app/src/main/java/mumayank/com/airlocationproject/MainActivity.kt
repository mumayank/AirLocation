package mumayank.com.airlocationproject

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import mumayank.com.airlocationlibrary.AirLocation
import mumayank.com.airlocationproject.databinding.MainActivityBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    private val airLocation = AirLocation(this, object : AirLocation.Callback {
        override fun onSuccess(locations: ArrayList<Location>) {
            binding.progressBar.visibility = View.GONE
            var string = "\n"
            for (location in locations) {
                string = "${location.longitude}, ${location.latitude}\n$string"
            }
            string = "$string${binding.textView2.text}"
            binding.textView2.text = string
        }

        override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@MainActivity, locationFailedEnum.name, Toast.LENGTH_SHORT)
                .show()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.GONE

        binding.button2.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            airLocation.start()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        airLocation.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}