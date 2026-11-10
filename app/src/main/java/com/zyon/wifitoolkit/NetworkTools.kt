package com.zyon.wifitoolkit

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class HostResult(val address: String, val reachable: Boolean)
data class PortResult(val port: Int, val open: Boolean)

object NetworkTools {
    // Checks a user-supplied host only; it does not scan the public internet.
    fun checkPorts(host: String, ports: List<Int>, timeoutMs: Int = 700): List<PortResult> {
        require(ports.all { it in 1..65535 }) { "Ports must be between 1 and 65535." }
        return ports.map { port ->
            val open = try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, port), timeoutMs)
                    true
                }
            } catch (_: Exception) { false }
            PortResult(port, open)
        }
    }

    // Discovers responsive addresses in the current /24 only.
    fun discoverLocal24(ip: String): List<HostResult> {
        val parts = ip.split(".")
        require(parts.size == 4 && parts.all { part -> part.toIntOrNull()?.let { value -> value in 0..255 } == true }) { "Enter a valid IPv4 address." }
        val prefix = parts.take(3).joinToString(".")
        val pool = Executors.newFixedThreadPool(32)
        return try {
            val tasks = (1..254).map { last ->
                pool.submit<HostResult> {
                    val address = "$prefix.$last"
                    val reachable = try { InetAddress.getByName(address).isReachable(350) } catch (_: Exception) { false }
                    HostResult(address, reachable)
                }
            }
            tasks.mapNotNull { try { it.get(3, TimeUnit.SECONDS).takeIf { r -> r.reachable } } catch (_: Exception) { null } }
        } finally {
            pool.shutdownNow()
        }
    }
}
