package mumayank.com.airlocationlibrary

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings

class NetworkUtil {

    companion object {

        @JvmStatic
        fun isNetworkAvailable(appContext: Context): Boolean {
            val connectivityManager =
                appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activteNetworkInfo = connectivityManager.activeNetworkInfo
            return activteNetworkInfo != null && activteNetworkInfo.isConnected
        }

        @JvmStatic
        fun isInFlightMode(appContext: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return Settings.System.getInt(
                    appContext.contentResolver,
                    Settings.System.AIRPLANE_MODE_ON,
                    0
                ) != 0;
            } else {
                return Settings.Global.getInt(
                    appContext.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON,
                    0
                ) != 0;
            }
        }

        @JvmStatic
        fun changeWifiState(appContext: Context, shouldWifiBeEnabled: Boolean) {
            val wifiManager =
                appContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = shouldWifiBeEnabled
        }

    }

}