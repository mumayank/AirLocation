package mumayank.com.airlocationlibrary.helpers

import android.content.Context
import android.provider.Settings

class NetworkHelper {

    companion object {

        @JvmStatic
        fun isInFlightMode(appContext: Context): Boolean {
            return Settings.Global.getInt(
                appContext.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
        }

    }

}