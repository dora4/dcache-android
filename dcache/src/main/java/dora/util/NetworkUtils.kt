package dora.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.util.*

object NetworkUtils {
    fun checkNetwork(context: Context): Boolean {
        val networkInfo = getActiveNetworkInfo(context)
        return networkInfo != null && networkInfo.isConnected
    }

    fun isWifiConnected(context: Context): Boolean {
        val networkInfo = getNetworkInfo(context, ConnectivityManager.TYPE_WIFI)
        return if (networkInfo != null) {
            networkInfo.isAvailable && networkInfo.isConnected
        } else false
    }

    fun isMobileConnected(context: Context): Boolean {
        val networkInfo = getNetworkInfo(context, ConnectivityManager.TYPE_MOBILE)
        return if (networkInfo != null) {
            networkInfo.isAvailable && networkInfo.isConnected
        } else false
    }

    private fun getNetworkInfo(context: Context, networkType: Int): NetworkInfo? {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkInfo(networkType)
    }

    private fun getActiveNetworkInfo(context: Context): NetworkInfo? {
        val connectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    fun getApnType(context: Context): ApnType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo ?: return ApnType.NONE
        val type = networkInfo.type
        if (type == ConnectivityManager.TYPE_MOBILE) {
            return if (networkInfo.extraInfo.toLowerCase(Locale.getDefault()) == "cmnet") {
                ApnType.CMNET
            } else {
                ApnType.CMWAP
            }
        } else if (type == ConnectivityManager.TYPE_WIFI) {
            return ApnType.WIFI
        }
        return ApnType.NONE
    }

    enum class ApnType {
        WIFI, CMNET, CMWAP, NONE
    }
}