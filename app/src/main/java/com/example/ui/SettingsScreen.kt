package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DnsProvider
import com.example.model.VpnProtocol
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDns by viewModel.selectedDns.collectAsState()
    val customDnsIp by viewModel.customDnsIp.collectAsState()
    val selectedProtocol by viewModel.selectedProtocol.collectAsState()
    val killSwitch by viewModel.killSwitch.collectAsState()
    val autoConnect by viewModel.autoConnectOnWifi.collectAsState()
    val diagnosticResult by viewModel.diagnosticResult.collectAsState()
    val splitApps by viewModel.splitTunnelApps.collectAsState()

    var showDnsDialog by remember { mutableStateOf(false) }
    var showProtocolDialog by remember { mutableStateOf(false) }
    var showSplitTunnelSection by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = "الأدوات والإعدادات المتقدمة",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "تخصيص أمان الشبكة، حماية الـ DNS، واختبار سرعة الاستجابة",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Diagnostic / Speed Test Card
        DiagnosticsCard(
            diagnosticResult = diagnosticResult,
            onRunDiagnostics = { viewModel.runDiagnostics() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Security Toggles
        Text(
            text = "الأمان والخصوصية",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Kill Switch
                SettingToggleItem(
                    icon = Icons.Default.Shield,
                    title = "مفتاح الإيقاف التلقائي (Kill Switch)",
                    description = "حظر حركة الإنترنت فوراً إذا انقطع اتصال الـ VPN فجأة لمنع تسريب البيانات.",
                    checked = killSwitch,
                    onCheckedChange = { viewModel.toggleKillSwitch(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Auto Connect
                SettingToggleItem(
                    icon = Icons.Default.Wifi,
                    title = "الاتصال التلقائي على شبكات Wi-Fi",
                    description = "تفعيل الـ VPN تلقائياً عند الاتصال بشبكة واي فاي جديدة أو غير موثوقة.",
                    checked = autoConnect,
                    onCheckedChange = { viewModel.toggleAutoConnect(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Protocol & DNS
        Text(
            text = "البروتوكول وخوادم DNS",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Protocol selector item
                SettingClickableItem(
                    icon = Icons.Default.NetworkCheck,
                    title = "بروتوكول الـ VPN",
                    subtitle = "${selectedProtocol.displayName} (${selectedProtocol.descAr})",
                    onClick = { showProtocolDialog = !showProtocolDialog }
                )

                AnimatedVisibility(visible = showProtocolDialog) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        VpnProtocol.values().forEach { protocol ->
                            val isSelected = selectedProtocol == protocol
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) DarkCardElevated else DarkBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyanPrimary else DarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.setProtocol(protocol)
                                        showProtocolDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setProtocol(protocol)
                                            showProtocolDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = protocol.displayName,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = protocol.descAr,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DNS Selector item
                SettingClickableItem(
                    icon = Icons.Default.Dns,
                    title = "خادم الـ DNS وحماية التسريب",
                    subtitle = "${selectedDns.title} (${selectedDns.descAr})",
                    onClick = { showDnsDialog = !showDnsDialog }
                )

                AnimatedVisibility(visible = showDnsDialog) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        DnsProvider.values().forEach { dns ->
                            val isSelected = selectedDns == dns
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) DarkCardElevated else DarkBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) CyanPrimary else DarkBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.setDns(dns)
                                        showDnsDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.setDns(dns)
                                            showDnsDialog = false
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = dns.title,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = dns.descAr,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Split Tunneling (تجاوز التطبيقات)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingClickableItem(
                    icon = Icons.Default.Apps,
                    title = "تجاوز التطبيقات (Split Tunneling)",
                    subtitle = "تحديد التطبيقات المستثناة من المرور عبر نفق الـ VPN المشفر",
                    onClick = { showSplitTunnelSection = !showSplitTunnelSection }
                )

                AnimatedVisibility(visible = showSplitTunnelSection) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "حدد التطبيقات التي ترغب في استثنائها من الـ VPN (ستتصل بالإنترنت المحلي مباشرة):",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (splitApps.isEmpty()) {
                            Text(
                                text = "جارٍ تحميل قائمة تطبيقات الهاتف...",
                                fontSize = 12.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            splitApps.take(15).forEach { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.appName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = app.packageName,
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Checkbox(
                                        checked = app.isBypassed,
                                        onCheckedChange = { viewModel.toggleAppBypass(app.packageName) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = CyanPrimary,
                                            checkmarkColor = DarkBg
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DiagnosticsCard(
    diagnosticResult: com.example.model.DiagnosticResult,
    onRunDiagnostics: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "تشخيص الاتصال واختبار السرعة",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "فحص استجابة الخادم وتسريب الـ DNS الحقيقي",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onRunDiagnostics,
                    enabled = !diagnosticResult.isTesting,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("run_diagnostics_button")
                ) {
                    if (diagnosticResult.isTesting) {
                        CircularProgressIndicator(
                            color = DarkBg,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "فحص الآن",
                            color = DarkBg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (diagnosticResult.pingMs > 0 || diagnosticResult.isTesting) {
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        DiagItem("زمن الاستجابة (Ping):", "${diagnosticResult.pingMs} ms", EmeraldGlow)
                        DiagItem("التذبذب (Jitter):", "${diagnosticResult.jitterMs} ms", CyanGlow)
                        DiagItem("فقدان الحزم (Packet Loss):", "${diagnosticResult.packetLossPercent}%", EmeraldGlow)
                        DiagItem("IP الخارجي المكتشف:", diagnosticResult.publicIp, TextPrimary)
                        DiagItem("الدولة المكتشفة:", diagnosticResult.countryDetected, TextPrimary)
                        DiagItem("حالة أمان DNS:", diagnosticResult.dnsStatus, EmeraldGlow)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagItem(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanPrimary,
                checkedTrackColor = CyanPrimary.copy(alpha = 0.4f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkCardElevated
            )
        )
    }
}

@Composable
private fun SettingClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
