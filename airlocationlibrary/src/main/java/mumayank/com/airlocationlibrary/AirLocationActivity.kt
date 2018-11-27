package mumayank.com.airlocationlibrary

import android.content.Intent
import android.support.v7.app.AppCompatActivity

import java.util.ArrayList

open class AirLocationActivity : AppCompatActivity() {

    var airLocation: AirLocation? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (airLocation != null) {
            val grandResultsArrayList = ArrayList<Int>()
            for (grantResult in grantResults) {
                grandResultsArrayList.add(grantResult)
            }
            airLocation!!.onRequestPermissionsResult(requestCode, grandResultsArrayList)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (airLocation != null) {
            airLocation!!.onActivityResult(requestCode, resultCode)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}