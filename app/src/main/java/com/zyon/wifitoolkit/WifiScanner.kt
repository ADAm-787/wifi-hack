package com.zyon.wifitoolkit

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val frequency: Int,
    val capabilities: String
)

class WifiScanner(private val context: Context) {
    @Suppress("DEPRECATION")
    fun scan(): List<WifiNetwork> {
        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return manager.scanResults
            .filter { it.SSID.isNotBlank() }
            .distinctBy { it.BSSID }
            .sortedByDescending { it.level }
            .map {
                WifiNetwork(
                    ssid = it.SSID,
                    bssid = it.BSSID ?: "Unknown",
                    level = it.level,
                    frequency = it.frequency,
                    capabilities = it.capabilities ?: "Unknown"
                )
            }
    }

    fun requestScan(): Boolean {
        val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return manager.startScan()
    }
}
