package com.example.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.Collections
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

data class LocalServerStatus(
    val isRunning: Boolean,
    val ipAddress: String,
    val port: Int,
    val isVirtualOrLoopbackIp: Boolean,
    val errorMessage: String? = null
) {
    val url: String
        get() = "http://$ipAddress:$port"
}

/**
 * Lightweight built-in HTTP/1.1 server that serves `assets/index.html` over Wi-Fi or Android Hotspot
 * so patients in the same room can scan the QR code and open Siap Operasi in Chrome/Safari
 * without needing any external cloud server or internet connection.
 */
object LocalWebServer {

    private val running = AtomicBoolean(false)

    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var executor: ExecutorService? = null

    @Volatile
    private var cachedHtmlBytes: ByteArray = ByteArray(0)

    @Volatile
    private var activePort: Int = 0

    @Synchronized
    fun start(context: Context, preferredPort: Int = 8080): LocalServerStatus {
        val (detectedIp, isVirtual) = detectLocalLanIp()

        if (running.get() && serverSocket?.isClosed == false && activePort > 0) {
            return LocalServerStatus(
                isRunning = true,
                ipAddress = detectedIp,
                port = activePort,
                isVirtualOrLoopbackIp = isVirtual
            )
        }

        stop()

        val htmlBytes = try {
            context.assets.open("index.html").use { it.readBytes() }
        } catch (e: Exception) {
            return LocalServerStatus(
                isRunning = false,
                ipAddress = detectedIp,
                port = 0,
                isVirtualOrLoopbackIp = isVirtual,
                errorMessage = "Gagal memuat file web lokal: ${e.localizedMessage ?: "Error"}"
            )
        }
        cachedHtmlBytes = htmlBytes

        val boundSocket = bindAvailablePort(preferredPort)
            ?: return LocalServerStatus(
                isRunning = false,
                ipAddress = detectedIp,
                port = 0,
                isVirtualOrLoopbackIp = isVirtual,
                errorMessage = "Tidak dapat membuka port jaringan lokal di perangkat ini."
            )

        serverSocket = boundSocket
        activePort = boundSocket.localPort
        running.set(true)

        val pool = Executors.newCachedThreadPool { runnable ->
            Thread(runnable, "SiapOperasiLocalWeb").apply { isDaemon = true }
        }
        executor = pool

        pool.execute {
            acceptLoop(boundSocket, pool)
        }

        return LocalServerStatus(
            isRunning = true,
            ipAddress = detectedIp,
            port = activePort,
            isVirtualOrLoopbackIp = isVirtual
        )
    }

    @Synchronized
    fun stop() {
        running.set(false)
        activePort = 0
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
        executor?.shutdownNow()
        executor = null
    }

    fun isRunning(): Boolean = running.get() && serverSocket?.isClosed == false

    private fun bindAvailablePort(preferredPort: Int): ServerSocket? {
        val candidates = listOf(preferredPort, 8081, 8082, 8088, 8888, 0)
        for (port in candidates) {
            try {
                val socket = ServerSocket(port)
                socket.reuseAddress = true
                return socket
            } catch (_: Exception) {
                // Try next port
            }
        }
        return null
    }

    private fun acceptLoop(socket: ServerSocket, pool: ExecutorService) {
        while (running.get() && !socket.isClosed) {
            try {
                val client = socket.accept()
                pool.execute { handleClient(client) }
            } catch (_: SocketException) {
                break
            } catch (_: Exception) {
                if (!running.get()) break
            }
        }
    }

    private fun handleClient(client: Socket) {
        client.use { sock ->
            try {
                sock.soTimeout = 5000
                val reader = BufferedReader(InputStreamReader(sock.getInputStream(), Charsets.UTF_8))
                val requestLine = reader.readLine() ?: return
                // Drain remaining request headers up to blank line
                while (true) {
                    val header = reader.readLine() ?: break
                    if (header.isEmpty()) break
                }

                val parts = requestLine.split(' ')
                val method = parts.getOrNull(0)?.uppercase().orEmpty()
                val path = parts.getOrNull(1).orEmpty()
                val output = sock.getOutputStream()

                if (path.startsWith("/favicon.ico")) {
                    val noContent = "HTTP/1.1 204 No Content\r\nConnection: close\r\n\r\n"
                    output.write(noContent.toByteArray(Charsets.UTF_8))
                    output.flush()
                    return
                }

                val body = cachedHtmlBytes
                val headers = buildString {
                    append("HTTP/1.1 200 OK\r\n")
                    append("Content-Type: text/html; charset=utf-8\r\n")
                    append("Content-Length: ${body.size}\r\n")
                    append("Cache-Control: no-store, no-cache, must-revalidate\r\n")
                    append("Access-Control-Allow-Origin: *\r\n")
                    append("Connection: close\r\n\r\n")
                }
                output.write(headers.toByteArray(Charsets.UTF_8))
                if (method != "HEAD") {
                    output.write(body)
                }
                output.flush()
            } catch (_: Exception) {
                // Ignore client disconnect
            }
        }
    }

    /**
     * Detects the best LAN / Android Tethering Hotspot IPv4 address.
     * Returns (ipAddress, isVirtualOrLoopbackIp).
     */
    fun detectLocalLanIp(): Pair<String, Boolean> {
        data class Candidate(val ip: String, val score: Int, val isVirtual: Boolean)
        val candidates = mutableListOf<Candidate>()

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            if (interfaces != null) {
                for (nif in Collections.list(interfaces)) {
                    if (!nif.isUp || nif.isLoopback) continue
                    val name = nif.name.lowercase()
                    // Skip cellular WAN interfaces (rmnet/ccmni) when looking for local Wi-Fi/Hotspot
                    val isCellular = name.startsWith("rmnet") || name.startsWith("ccmni") || name.startsWith("pdp")

                    for (addr in Collections.list(nif.inetAddresses)) {
                        if (addr !is Inet4Address || addr.isLoopbackAddress) continue
                        val ip = addr.hostAddress ?: continue
                        val isEmulatorNat = ip.startsWith("10.0.2.") || ip.startsWith("10.0.3.")

                        var score = 10
                        if (name.contains("ap") || name.contains("wlan") || name.contains("swlan") || name.contains("rndis")) {
                            score += 50
                        } else if (name.startsWith("eth") || name.startsWith("en")) {
                            score += 30
                        }
                        if (ip.startsWith("192.168.43.") || ip.startsWith("192.168.49.")) {
                            // Classic Android Wi-Fi Hotspot subnet
                            score += 40
                        } else if (ip.startsWith("192.168.")) {
                            score += 30
                        } else if (ip.startsWith("172.")) {
                            score += 20
                        }
                        if (isCellular) score -= 40
                        if (isEmulatorNat) score -= 25

                        candidates.add(Candidate(ip = ip, score = score, isVirtual = isEmulatorNat))
                    }
                }
            }
        } catch (_: Exception) {
        }

        val best = candidates.maxByOrNull { it.score }
        return if (best != null) {
            best.ip to best.isVirtual
        } else {
            "127.0.0.1" to true
        }
    }
}
