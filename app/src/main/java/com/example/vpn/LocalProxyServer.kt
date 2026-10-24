package com.example.vpn

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

class LocalProxyServer(
    private val onClientCountChanged: (Int) -> Unit = {}
) {
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeConnections = AtomicInteger(0)

    val isRunning: Boolean
        get() = serverSocket != null && !(serverSocket?.isClosed ?: true)

    fun start(port: Int = 8080): Boolean {
        if (isRunning) return true
        return try {
            val server = ServerSocket()
            server.reuseAddress = true
            server.bind(InetSocketAddress("0.0.0.0", port))
            serverSocket = server

            serverJob = scope.launch {
                Log.d("LocalProxyServer", "Proxy server started on port $port")
                while (isActive && !server.isClosed) {
                    try {
                        val clientSocket = server.accept()
                        handleClient(clientSocket)
                    } catch (e: Exception) {
                        if (!server.isClosed) {
                            Log.e("LocalProxyServer", "Accept error", e)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("LocalProxyServer", "Failed to start proxy on port $port", e)
            false
        }
    }

    private fun handleClient(clientSocket: Socket) {
        scope.launch {
            val count = activeConnections.incrementAndGet()
            onClientCountChanged(count)
            try {
                clientSocket.soTimeout = 30000
                val inFromClient = clientSocket.getInputStream()
                val outToClient = clientSocket.getOutputStream()

                val initialBuffer = ByteArray(4096)
                val bytesRead = inFromClient.read(initialBuffer)
                if (bytesRead <= 0) {
                    clientSocket.close()
                    return@launch
                }

                val requestHeader = String(initialBuffer, 0, bytesRead)
                val lines = requestHeader.split("\r\n")
                if (lines.isEmpty()) {
                    clientSocket.close()
                    return@launch
                }

                val firstLine = lines[0].split(" ")
                if (firstLine.size >= 2) {
                    val method = firstLine[0]
                    val urlOrHost = firstLine[1]

                    if (method.equals("CONNECT", ignoreCase = true)) {
                        // HTTPS tunnel
                        val parts = urlOrHost.split(":")
                        val host = parts[0]
                        val port = if (parts.size > 1) parts[1].toIntOrNull() ?: 443 else 443

                        val remoteSocket = Socket()
                        remoteSocket.connect(InetSocketAddress(host, port), 10000)

                        // Respond to client: HTTP/1.1 200 Connection Established
                        val response = "HTTP/1.1 200 Connection Established\r\n\r\n"
                        outToClient.write(response.toByteArray(Charsets.UTF_8))
                        outToClient.flush()

                        // Pipe bidirectional
                        val inFromRemote = remoteSocket.getInputStream()
                        val outToRemote = remoteSocket.getOutputStream()

                        val job1 = launch { pipe(inFromClient, outToRemote) }
                        val job2 = launch { pipe(inFromRemote, outToClient) }

                        job1.join()
                        job2.join()
                        remoteSocket.close()
                    } else {
                        // Standard HTTP relay or proxy test ping
                        val hostLine = lines.firstOrNull { it.startsWith("Host:", ignoreCase = true) }
                        val hostVal = hostLine?.substringAfter(":")?.trim() ?: ""
                        val parts = hostVal.split(":")
                        val host = parts[0].ifEmpty { "1.1.1.1" }
                        val port = if (parts.size > 1) parts[1].toIntOrNull() ?: 80 else 80

                        val remoteSocket = Socket()
                        remoteSocket.connect(InetSocketAddress(host, port), 10000)
                        val outToRemote = remoteSocket.getOutputStream()
                        outToRemote.write(initialBuffer, 0, bytesRead)
                        outToRemote.flush()

                        val inFromRemote = remoteSocket.getInputStream()
                        val job1 = launch { pipe(inFromClient, outToRemote) }
                        val job2 = launch { pipe(inFromRemote, outToClient) }
                        job1.join()
                        job2.join()
                        remoteSocket.close()
                    }
                }
            } catch (_: Exception) {
                // Connection closed or timed out
            } finally {
                try {
                    clientSocket.close()
                } catch (_: Exception) {}
                val countAfter = activeConnections.decrementAndGet().coerceAtLeast(0)
                onClientCountChanged(countAfter)
            }
        }
    }

    private fun pipe(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(8192)
        try {
            var n: Int
            while (input.read(buffer).also { n = it } != -1) {
                output.write(buffer, 0, n)
                output.flush()
            }
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
        serverJob?.cancel()
        serverJob = null
        activeConnections.set(0)
        onClientCountChanged(0)
    }
}
