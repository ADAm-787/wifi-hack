package com.zyon.wifitoolkit

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object ReportGenerator {
    fun create(context: Context, networks: List<WifiNetwork>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val paint = Paint().apply { textSize = 12f }
        var y = 48f
        page.canvas.drawText("WiFi Security Toolkit - Scan Report", 40f, y, paint)
        y += 28f
        page.canvas.drawText("Networks found: ${networks.size}", 40f, y, paint)
        y += 28f
        networks.forEach { network ->
            val line = "${network.ssid.take(24)} | ${network.level} dBm | ${network.frequency} MHz"
            if (y > 800f) return@forEach
            page.canvas.drawText(line, 40f, y, paint)
            y += 20f
            val security = network.capabilities.take(70)
            if (y <= 800f) {
                page.canvas.drawText("Security: $security", 55f, y, paint)
                y += 22f
            }
        }
        document.finishPage(page)
        val file = File(context.getExternalFilesDir(null), "wifi_scan_report.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
