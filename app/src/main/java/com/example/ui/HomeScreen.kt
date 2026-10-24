package com.example.ui

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.vpn.NetworkUtils

@Composable
fun HomeScreen(
    viewModel: VpnViewModel,
    onPrepareRequired: (Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val server by viewModel.selectedServer.collectAsState()
    val stats by viewModel.statistics.collectAsState()
    val scrollState = rememberScrollState()

    val isConnected = connectionStatus == ConnectionStatus.CONNECTED
    val isConnecting = connectionStatus == ConnectionStatus.CONNECTING

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Top Badge
        StatusTopBadge(connectionStatus = connectionStatus)

        Spacer(modifier = Modifier.height(24.dp))

        // Large Cyber Connect Button
        ConnectButtonSection(
            connectionStatus = connectionStatus,
            onClick = {
                viewModel.toggleConnection(context, onPrepareRequired)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Connection Timer & Live Ping (if connected)
        if (isConnected) {
            ConnectionLiveStatsBar(
                durationSec = stats.durationSeconds,
                pingMs = server.latencyMs,
                virtualIp = stats.virtualIp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active Selected Server Card
        ActiveServerCard(
            server = server,
            isConnected = isConnected,
            onChangeServerClick = {
                viewModel.setTab(VpnTab.SERVERS)
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Traffic Speed & Data Transferred
        TrafficStatisticsGrid(stats = stats, isConnected = isConnected)

        Spacer(modifier = Modifier.height(20.dp))

        // Connection & Hotspot Sharing Banner
        HotspotShareBanner(
            isConnected = isConnected,
            onShareClick = {
                viewModel.setTab(VpnTab.SHARE_GUIDE)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatusTopBadge(connectionStatus: ConnectionStatus) {
    val (bgColor, textColor, text, icon) = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> Quad(
            EmeraldSuccess.copy(alpha = 0.15f),
            EmeraldGlow,
            "محمي ومشفر بالكامل (VPN Active)",
            Icons.Default.Security
        )
        ConnectionStatus.CONNECTING -> Quad(
            CyanPrimary.copy(alpha = 0.15f),
            CyanGlow,
            "جارٍ تأسيس النفق المشفر…",
            Icons.Default.Lock
        )
        ConnectionStatus.DISCONNECTING -> Quad(
            Color(0xFFF59E0B).copy(alpha = 0.15f),
            Color(0xFFF59E0B),
            "جارٍ قطع الاتصال…",
            Icons.Default.PowerSettingsNew
        )
        ConnectionStatus.ERROR -> Quad(
            Color(0xFFEF4444).copy(alpha = 0.15f),
            Color(0xFFEF4444),
            "حدث خطأ في الاتصال",
            Icons.Default.PowerSettingsNew
        )
        ConnectionStatus.DISCONNECTED -> Quad(
            DarkCardElevated,
            TextSecondary,
            "غير متصل • حركة البيانات غير مشفرة",
            Icons.Default.Lock
        )
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.35f)),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ConnectButtonSection(
    connectionStatus: ConnectionStatus,
    onClick: () -> Unit
) {
    val isConnected = connectionStatus == ConnectionStatus.CONNECTED
    val isConnecting = connectionStatus == ConnectionStatus.CONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected || isConnecting) 1.14f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryColor by animateColorAsState(
        targetValue = when (connectionStatus) {
            ConnectionStatus.CONNECTED -> EmeraldSuccess
            ConnectionStatus.CONNECTING -> CyanPrimary
            else -> CyanPrimary
        },
        animationSpec = tween(500),
        label = "color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(210.dp)
        ) {
            // Animated Pulse Outer Ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = if (isConnected) 0.18f else 0.08f))
            )

            // Second Ring
            Box(
                modifier = Modifier
                    .size(166.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.25f),
                                DarkCard
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.8f),
                                primaryColor.copy(alpha = 0.2f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Main Clickable Core Button
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            if (isConnected) {
                                listOf(EmeraldSuccess, Color(0xFF047857))
                            } else {
                                listOf(DarkCardElevated, DarkCard)
                            }
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = if (isConnected) EmeraldGlow else CyanPrimary.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onClick)
                    .testTag("vpn_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        color = CyanPrimary,
                        modifier = Modifier.size(54.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "اتصال VPN",
                        tint = if (isConnected) Color.White else CyanPrimary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (connectionStatus) {
                ConnectionStatus.CONNECTED -> "متصل وآمن بنجاح"
                ConnectionStatus.CONNECTING -> "جارٍ إنشاء الاتصال المشفر..."
                ConnectionStatus.DISCONNECTING -> "جارٍ إنهاء الاتصال..."
                ConnectionStatus.ERROR -> "فشل الاتصال، اضغط للمحاولة"
                ConnectionStatus.DISCONNECTED -> "اضغط للاتصال السريع"
            },
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (isConnected) "انقر للإيقاف وقطع الاتصال" else "تأمين وحماية كاملة لحركة البيانات",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ConnectionLiveStatsBar(
    durationSec: Long,
    pingMs: Int,
    virtualIp: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Duration
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "مدة الاتصال",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = NetworkUtils.formatDuration(durationSec),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Virtual IP
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = EmeraldGlow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "IP الافتراضي",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = virtualIp,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Ping
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = EmeraldGlow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "زمن الاستجابة",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "$pingMs ms",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveServerCard(
    server: com.example.model.VpnServer,
    isConnected: Boolean,
    onChangeServerClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) EmeraldSuccess.copy(alpha = 0.5f) else DarkBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onChangeServerClick)
            .testTag("select_server_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Flag Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = server.flagEmoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = server.countryNameAr,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = server.protocol.displayName,
                                color = CyanGlow,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "${server.cityAr} • ${server.ipAddress}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Change button pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تغيير",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrafficStatisticsGrid(
    stats: com.example.model.VpnStatistics,
    isConnected: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Download Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "سرعة التحميل", fontSize = 12.sp, color = TextMuted)
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isConnected) NetworkUtils.formatSpeed(stats.downloadSpeedKbps) else "0 KB/s",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "الإجمالي: ${if (isConnected) NetworkUtils.formatBytes(stats.bytesIn) else "0 B"}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Upload Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "سرعة الرفع", fontSize = 12.sp, color = TextMuted)
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isConnected) NetworkUtils.formatSpeed(stats.uploadSpeedKbps) else "0 KB/s",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "الإجمالي: ${if (isConnected) NetworkUtils.formatBytes(stats.bytesOut) else "0 B"}",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun HotspotShareBanner(
    isConnected: Boolean,
    onShareClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = DarkCardElevated
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShareClick)
            .testTag("hotspot_share_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "طرق الاتصال ومشاركة الـ VPN",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اتصال الهاتف، مشاركة الـ Hotspot مع الأجهزة، وتكوين WireGuard",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
