package com.example.model

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

enum class VpnProtocol(val displayName: String, val descAr: String) {
    WIREGUARD("WireGuard", "الأسرع والأحدث، استهلاك منخفض للبطارية"),
    OPENVPN_UDP("OpenVPN (UDP)", "ممتاز للألعاب والاتصال السريع"),
    OPENVPN_TCP("OpenVPN (TCP)", "الأكثر استقراراً في تخطي الحجب والجدران النارية"),
    SHADOWSOCKS("Shadowsocks", "تشفير خفي لتجاوز القيود الصارمة")
}

enum class DnsProvider(val title: String, val primaryIp: String, val secondaryIp: String, val descAr: String) {
    CLOUDFLARE("Cloudflare (1.1.1.1)", "1.1.1.1", "1.0.0.1", "سرعة فائقة مع حماية خصوصية صارمة"),
    GOOGLE("Google Public DNS", "8.8.8.8", "8.8.4.4", "استقرار عالمي وسرعة استجابة عالية"),
    ADGUARD("AdGuard DNS", "94.140.14.14", "94.140.15.15", "حجب الإعلانات والمتعقبات والمواقع الخبيثة"),
    QUAD9("Quad9 Security", "9.9.9.9", "149.112.112.112", "حماية متقدمة من التهديدات والتصيد"),
    CUSTOM("مخصص (Custom DNS)", "", "", "حدد عناوين DNS يدويًا")
}

enum class ServerCategory(val labelAr: String, val labelEn: String) {
    ALL("الكل", "All"),
    FASTEST("الأسرع", "Fastest"),
    RECOMMENDED("موصى به", "Recommended"),
    STREAMING("البث والوسائط", "Streaming")
}

data class VpnServer(
    val id: String,
    val countryNameAr: String,
    val countryNameEn: String,
    val cityAr: String,
    val cityEn: String,
    val countryCode: String,
    val flagEmoji: String,
    val ipAddress: String,
    val port: Int = 51820,
    val latencyMs: Int,
    val loadPercent: Int,
    val protocol: VpnProtocol = VpnProtocol.WIREGUARD,
    val isRecommended: Boolean = false,
    val category: ServerCategory = ServerCategory.ALL,
    val publicKey: String = "S4f3Sh13ldVpnK3yEx4mpl3Publ1cK3y1234567890=",
    val virtualSubnet: String = "10.8.0.2/24",
    val streamingSupported: Boolean = true
)

data class VpnStatistics(
    val durationSeconds: Long = 0L,
    val bytesIn: Long = 0L,
    val bytesOut: Long = 0L,
    val downloadSpeedKbps: Float = 0f,
    val uploadSpeedKbps: Float = 0f,
    val virtualIp: String = "10.8.0.2",
    val connectedAt: Long = 0L
)

data class SplitTunnelApp(
    val packageName: String,
    val appName: String,
    val isBypassed: Boolean = false
)

data class ProxyServerInfo(
    val isRunning: Boolean = false,
    val hostIp: String = "192.168.43.1",
    val port: Int = 8080,
    val connectedClients: Int = 0
)

data class DiagnosticResult(
    val pingMs: Int = 0,
    val jitterMs: Int = 0,
    val packetLossPercent: Int = 0,
    val publicIp: String = "",
    val countryDetected: String = "",
    val dnsStatus: String = "محمي ومشفر",
    val isTesting: Boolean = false
)
