package com.example.vpn

import com.example.model.DnsProvider
import com.example.model.VpnServer
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Locale

object NetworkUtils {

    fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "192.168.43.1"
            for (intf in interfaces) {
                // Check hotspot, wifi, or ethernet interfaces
                val name = intf.name.lowercase(Locale.ROOT)
                val addresses = intf.inetAddresses
                for (addr in addresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val hostAddress = addr.hostAddress ?: continue
                        if (name.contains("ap") || name.contains("wlan") || name.contains("rndis")) {
                            return hostAddress
                        }
                    }
                }
            }
            // Second pass: any non-loopback ipv4
            val allInterfaces = NetworkInterface.getNetworkInterfaces() ?: return "192.168.43.1"
            for (intf in allInterfaces) {
                for (addr in intf.inetAddresses) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "192.168.43.1"
                    }
                }
            }
        } catch (_: Exception) {
            // fallback
        }
        return "192.168.43.1"
    }

    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun formatSpeed(kbps: Float): String {
        return if (kbps >= 1024f) {
            String.format(Locale.US, "%.1f MB/s", kbps / 1024f)
        } else {
            String.format(Locale.US, "%.0f KB/s", kbps)
        }
    }

    fun generateWireGuardConfig(server: VpnServer, dns: DnsProvider): String {
        val dnsAddress = if (dns.primaryIp.isNotEmpty()) {
            "${dns.primaryIp}, ${dns.secondaryIp}"
        } else {
            "1.1.1.1, 8.8.8.8"
        }
        return """
[Interface]
# SafeShield VPN Client Config (${server.countryNameEn} - ${server.cityEn})
PrivateKey = aBcDeFgHiJkLmNoPqRsTuVwXyZ1234567890=
Address = ${server.virtualSubnet}
DNS = $dnsAddress
MTU = 1420

[Peer]
# Server: ${server.countryNameEn} (${server.ipAddress})
PublicKey = ${server.publicKey}
Endpoint = ${server.ipAddress}:${server.port}
AllowedIPs = 0.0.0.0/0, ::/0
PersistentKeepalive = 25
""".trimIndent()
    }

    fun generateOpenVpnConfig(server: VpnServer): String {
        return """
client
dev tun
proto udp
remote ${server.ipAddress} 1194
resolv-retry infinite
nobind
persist-key
persist-tun
cipher AES-256-GCM
auth SHA256
key-direction 1
verb 3
mute-replay-warnings
<ca>
-----BEGIN CERTIFICATE-----
MIIB/zCCAaWgAwIBAgIUQSafeShieldRootCA00112233445566778899AABBCCDDEE
-----END CERTIFICATE-----
</ca>
auth-user-pass
""".trimIndent()
    }
}
