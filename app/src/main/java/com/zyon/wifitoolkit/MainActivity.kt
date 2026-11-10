package com.zyon.wifitoolkit

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.app.Activity
import android.view.Gravity
import android.graphics.Typeface

class MainActivity : Activity() {
    private val scanner by lazy { WifiScanner(this) }
    private val networks = mutableListOf<WifiNetwork>()
    private lateinit var output: TextView
    private val requestCode = 41

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF101820.toInt()
        window.navigationBarColor = 0xFF101820.toInt()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(0xFF101820.toInt())
        }
        val title = TextView(this).apply {
            text = "WiFi Security Toolkit"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Android • No Root • Local network diagnostics"
            textSize = 13f
            setTextColor(0xFF9FB3C8.toInt())
        }
        output = TextView(this).apply {
            text = "Ready. Grant nearby Wi-Fi/location permission, then scan."
            textSize = 14f
            setTextColor(0xFFE6EDF3.toInt())
            setPadding(0, 18, 0, 18)
        }
        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(makeButton("Scan nearby Wi-Fi networks") { ensurePermissionsAndScan() })
        layout.addView(makeButton("Export PDF report") {
            try {
                val file = ReportGenerator.create(this, networks)
                output.text = "Report saved inside app storage:\n${file.absolutePath}"
            } catch (e: Exception) {
                toast("Report error: ${e.message}")
            }
        })
        layout.addView(output)
        val scroll = ScrollView(this).apply { addView(layout) }
        setContentView(scroll)
    }

    private fun makeButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }

    private fun ensurePermissionsAndScan() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.NEARBY_WIFI_DEVICES
        val missing = permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), requestCode)
        } else {
            scanNetworks()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode && grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }) scanNetworks()
        else toast("Wi-Fi scan permission was not granted.")
    }

    @Suppress("DEPRECATION")
    private fun scanNetworks() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationEnabled = if (Build.VERSION.SDK_INT >= 28) locationManager.isLocationEnabled
                              else locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!locationEnabled) {
            output.text = "Turn on Location in Android settings, then scan again. Android requires it for nearby Wi-Fi scan results."
            return
        }
        scanner.requestScan()
        networks.clear()
        networks.addAll(scanner.scan())
        output.text = if (networks.isEmpty()) {
            "No scan results yet. Wait a few seconds and tap Scan again. Android may throttle repeated scans."
        } else {
            buildString {
                appendLine("Found ${networks.size} networks")
                appendLine("────────────────────────")
                networks.forEachIndexed { index, n ->
                    appendLine("${index + 1}. ${n.ssid}")
                    appendLine("   ${n.level} dBm • ${n.frequency} MHz")
                    appendLine("   ${n.capabilities}")
                    SecurityAnalyzer.analyze(n).forEach { appendLine("   • ${it.label}: ${it.detail}") }
                    appendLine()
                }
            }
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
