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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.model.VpnServer
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
import com.example.vpn.NetworkUtils

@Composable
fun ShareGuideScreen(
    viewModel: VpnViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val server by viewModel.selectedServer.collectAsState()
    val status by viewModel.connectionStatus.collectAsState()
    val proxyInfo by viewModel.proxyInfo.collectAsState()
    val selectedDns by viewModel.selectedDns.collectAsState()
    val diagnosticResult by viewModel.diagnosticResult.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // Country Header Summary
        SelectedCountryHeader(server = server, status = status)

        Spacer(modifier = Modifier.height(20.dp))

        // Title and intro
        Text(
            text = "طرق الاتصال والمشاركة المتاحة",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اختر الطريقة التي تفضلها لتشغيل الاتصال على هاتفك أو مشاركته مع أجهزة أخرى:",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // METHOD 1: Direct Android Phone Connection
        DirectAndroidMethodCard(
            server = server,
            status = status,
            onTestPingClick = { viewModel.runDiagnostics() },
            diagnosticResult = diagnosticResult
        )

        Spacer(modifier = Modifier.height(16.dp))

        // METHOD 2: Hotspot & Local Proxy Sharing
        HotspotProxyMethodCard(
            proxyInfo = proxyInfo,
            onToggleProxy = { enable ->
                viewModel.toggleProxy(context, enable, 8080)
            },
            onCopy = { label, text ->
                viewModel.copyToClipboard(context, label, text)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // METHOD 3: WireGuard Configuration Profile & QR
        WireGuardConfigMethodCard(
            server = server,
            dns = selectedDns,
            onCopyConfig = { text ->
                viewModel.copyToClipboard(context, "WireGuard Config", text)
            },
            onShareConfig = { text ->
                viewModel.shareConfig(context, "SafeShield WireGuard Config - ${server.countryNameEn}", text)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // METHOD 4: Android Native Settings (Manual Setup)
        AndroidSystemManualCard(
            server = server,
            onCopy = { label, text ->
                viewModel.copyToClipboard(context, label, text)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SelectedCountryHeader(
    server: VpnServer,
    status: ConnectionStatus
) {
    val isConnected = status == ConnectionStatus.CONNECTED

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isConnected) EmeraldSuccess.copy(alpha = 0.5f) else DarkBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = server.flagEmoji, fontSize = 32.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "الدولة المحددة: ${server.countryNameAr}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${server.cityAr} (${server.countryNameEn}) • IP: ${server.ipAddress}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = if (isConnected) "الحالة: متصل ويعمل داخل هاتف الأندرويد الآن" else "الحالة: غير متصل (اضغط على زر الاتصال لتفعيله)",
                    color = if (isConnected) EmeraldGlow else CyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DirectAndroidMethodCard(
    server: VpnServer,
    status: ConnectionStatus,
    onTestPingClick: () -> Unit,
    diagnosticResult: com.example.model.DiagnosticResult
) {
    var expanded by remember { mutableStateOf(true) }
    val isConnected = status == ConnectionStatus.CONNECTED

    MethodContainer(
        icon = Icons.Default.PhoneAndroid,
        number = "1",
        title = "الاتصال المباشر على هذا الهاتف (أندرويد)",
        badge = if (isConnected) "مفعّل ونشط الآن" else "موصى به للأندرويد",
        badgeColor = if (isConnected) EmeraldSuccess else CyanPrimary,
        expanded = expanded,
        onToggleExpand = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = "يعمل الاتصال مباشرة داخل نظام الأندرويد عبر خدمة VpnService المشفرة. فور الضغط على زر الاتصال في الشاشة الرئيسية:",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            StepItem(
                stepNum = "✓",
                text = "يظهر رمز المفتاح 🔑 في شريط إشعارات الأندرويد، مؤكداً تشفير وحماية حركة البيانات."
            )
            StepItem(
                stepNum = "✓",
                text = "يتم توجيه جميع تطبيقات الهاتف (المتصفح، يوتيوب، الألعاب، السوشيال) تلقائياً عبر دولة ${server.countryNameAr}."
            )
            StepItem(
                stepNum = "✓",
                text = "حماية كاملة من تسريب الـ DNS وإخفاء عنوان IP الحقيقي بهوية مشفرة (${server.ipAddress})."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Diagnostic test action
            OutlinedButton(
                onClick = onTestPingClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("test_direct_ping_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (diagnosticResult.isTesting) "جارٍ فحص استجابة الخادم..." else "فحص استجابة الاتصال المباشر (Ping Test)",
                    fontSize = 13.sp
                )
            }

            if (diagnosticResult.pingMs > 0 && !diagnosticResult.isTesting) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldSuccess.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🟢 الاتصال سليم: زمن الاستجابة ${diagnosticResult.pingMs}ms • تم تأكيد الاتصال بدولة ${server.countryNameAr}",
                        color = EmeraldGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HotspotProxyMethodCard(
    proxyInfo: com.example.model.ProxyServerInfo,
    onToggleProxy: (Boolean) -> Unit,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    MethodContainer(
        icon = Icons.Default.WifiTethering,
        number = "2",
        title = "مشاركة الـ VPN عبر نقطة الاتصال (Hotspot Proxy)",
        badge = if (proxyInfo.isRunning) "البروكسي نشط" else "مشاركة الأجهزة",
        badgeColor = if (proxyInfo.isRunning) EmeraldSuccess else CyanPrimary,
        expanded = expanded,
        onToggleExpand = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = "أجهزة أندرويد لا تمرر الـ VPN لنقطة الاتصال Hotspot تلقائياً بدون بروكسي محلي. يتيح لك هذا الخيار المدمج تشغيل بروكسي فوري على هذا الهاتف لمشاركة الاتصال المشفر مع أي هاتف آخر أو لابتوب:",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Proxy Toggle Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "تشغيل خادم البروكسي المحلي",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (proxyInfo.isRunning) "يعمل الآن ويستقبل اتصالات الأجهزة" else "متوقف (اضغط للتفعيل)",
                            color = if (proxyInfo.isRunning) EmeraldGlow else TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = proxyInfo.isRunning,
                        onCheckedChange = onToggleProxy,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGlow,
                            checkedTrackColor = EmeraldSuccess.copy(alpha = 0.5f),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = DarkCard
                        ),
                        modifier = Modifier.testTag("proxy_server_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proxy Host & Port Display with Copy Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Host IP
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "اسم المضيف (Host IP)", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = proxyInfo.hostIp,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanGlow,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { onCopy("Proxy Host", proxyInfo.hostIp) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ المضيف",
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Port
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.weight(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "المنفذ (Port)", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${proxyInfo.port}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanGlow,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = { onCopy("Proxy Port", "${proxyInfo.port}") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "نسخ المنفذ",
                                tint = CyanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "خطوات التوصيل على الهاتف الآخر:",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            StepItem("1", "شغّل نقطة اتصال الهواتف المحمولة (Mobile Hotspot) من إعدادات هذا الهاتف.")
            StepItem("2", "اتصل بشبكة الواي فاي من الهاتف الآخر أو جهاز اللابتوب.")
            StepItem("3", "في الهاتف الآخر: افتح إعدادات Wi-Fi -> انقر مطولاً على اسم الشبكة -> خيارات متقدمة -> البروكسي: يدوي (Manual).")
            StepItem("4", "أدخل اسم المضيف: ${proxyInfo.hostIp} والمنفذ: ${proxyInfo.port} واضغط حفظ.")
            StepItem("5", "سيعمل إنترنت الهاتف الآخر كاملاً عبر نفس دولة الـ VPN المحددة!")
        }
    }
}

@Composable
private fun WireGuardConfigMethodCard(
    server: VpnServer,
    dns: com.example.model.DnsProvider,
    onCopyConfig: (String) -> Unit,
    onShareConfig: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val configText = remember(server, dns) {
        NetworkUtils.generateWireGuardConfig(server, dns)
    }

    MethodContainer(
        icon = Icons.Default.QrCode,
        number = "3",
        title = "تكوين WireGuard الجاهز (ملف .conf و QR)",
        badge = "للأجهزة الخارجية",
        badgeColor = CyanGlow,
        expanded = expanded,
        onToggleExpand = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = "إذا كنت تريد استخدام تطبيق WireGuard الرسمي على هاتف آخر، أو على جهاز الراوتر أو الكمبيوتر، يمكنك نسخ هذا التكوين الجاهز المخصص لدولة ${server.countryNameAr}:",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Code box preview
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = configText,
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onCopyConfig(configText) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_wireguard_config_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = DarkBg,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "نسخ التكوين", color = DarkBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onShareConfig(configText) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مشاركة الملف", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AndroidSystemManualCard(
    server: VpnServer,
    onCopy: (String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    MethodContainer(
        icon = Icons.Default.Tune,
        number = "4",
        title = "إعداد VPN اليدوي في ضبط نظام الأندرويد",
        badge = "بدون أي تطبيق",
        badgeColor = TextMuted,
        expanded = expanded,
        onToggleExpand = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Text(
                text = "يمكنك إضافة هذا الخادم مباشرة في إعدادات هاتف الأندرويد دون الحاجة لتطبيق يعمل في الخلفية:",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            StepItem("1", "افتح ضبط هاتف الأندرويد -> الاتصالات (أو الشبكة والإنترنت) -> المزيد من ضبط الاتصال -> VPN.")
            StepItem("2", "اضغط على زر الإضافة (+) لإنشاء ملف تعريف VPN جديد.")
            StepItem("3", "الاسم: SafeShield - ${server.countryNameAr}")
            StepItem("4", "النوع: IKEv2/IPSec PSK أو WireGuard.")
            StepItem("5", "عنوان الخادم: ${server.ipAddress}")

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onCopy("Server IP", server.ipAddress) },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = CyanPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "نسخ عنوان الخادم: ${server.ipAddress}",
                    color = CyanGlow,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MethodContainer(
    icon: ImageVector,
    number: String,
    title: String,
    badge: String,
    badgeColor: Color,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = badge,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "إخفاء" else "عرض",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                content()
            }
        }
    }
}

@Composable
private fun StepItem(stepNum: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = DarkCardElevated,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNum,
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
