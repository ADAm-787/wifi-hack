package com.zyon.wifitoolkit

data class SecurityFinding(val label: String, val detail: String)

object SecurityAnalyzer {
    fun analyze(network: WifiNetwork): List<SecurityFinding> {
        val caps = network.capabilities.uppercase()
        val findings = mutableListOf<SecurityFinding>()
        when {
            "WPA3" in caps -> findings += SecurityFinding("Strong protocol detected", "WPA3 advertisement found.")
            "WPA2" in caps -> findings += SecurityFinding("WPA2 detected", "Use a long, unique passphrase and keep router firmware updated.")
            "WEP" in caps -> findings += SecurityFinding("Legacy encryption", "WEP is obsolete. Change the router to WPA2-AES or WPA3.")
            "ESS" in caps && !("WPA" in caps || "WEP" in caps) ->
                findings += SecurityFinding("Open or unclear security", "The scan result does not advertise WPA/WPA2/WPA3. Verify router settings.")
            else -> findings += SecurityFinding("Security needs verification", "Android scan capabilities are not enough to fully audit router configuration.")
        }
        if ("WPS" in caps) findings += SecurityFinding("WPS advertised", "Disable WPS in the router settings if it is not needed.")
        return findings
    }
}
