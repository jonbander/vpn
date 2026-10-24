package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ConnectionStatus
import com.example.ui.HomeScreen
import com.example.ui.ServersScreen
import com.example.ui.SettingsScreen
import com.example.ui.ShareGuideScreen
import com.example.ui.VpnTab
import com.example.ui.VpnViewModel
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    vpnViewModel: VpnViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by vpnViewModel.currentTab.collectAsState()
    val connectionStatus by vpnViewModel.connectionStatus.collectAsState()

    // VPN Permission Launcher for Android System Dialog
    val vpnPrepareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            vpnViewModel.onVpnPermissionGranted(context)
        }
    }

    // Handle back button: return to Home if on secondary tab
    BackHandler(enabled = currentTab != VpnTab.HOME) {
        vpnViewModel.setTab(VpnTab.HOME)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            SafeShieldTopBar(
                connectionStatus = connectionStatus,
                currentTab = currentTab
            )
        },
        bottomBar = {
            SafeShieldBottomBar(
                currentTab = currentTab,
                onTabSelected = { vpnViewModel.setTab(it) }
            )
        },
        containerColor = DarkBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                VpnTab.HOME -> HomeScreen(
                    viewModel = vpnViewModel,
                    onPrepareRequired = { intent -> vpnPrepareLauncher.launch(intent) }
                )
                VpnTab.SHARE_GUIDE -> ShareGuideScreen(
                    viewModel = vpnViewModel
                )
                VpnTab.SERVERS -> ServersScreen(
                    viewModel = vpnViewModel
                )
                VpnTab.SETTINGS -> SettingsScreen(
                    viewModel = vpnViewModel
                )
            }
        }
    }
}

@Composable
fun SafeShieldTopBar(
    connectionStatus: ConnectionStatus,
    currentTab: VpnTab
) {
    val isConnected = connectionStatus == ConnectionStatus.CONNECTED

    Surface(
        color = DarkBg,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Shield Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            if (isConnected) listOf(EmeraldSuccess, EmeraldGlow)
                            else listOf(CyanPrimary, Color(0xFF0284C7))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = DarkBg,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SafeShield VPN",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = when (currentTab) {
                        VpnTab.HOME -> "الشبكة الافتراضية المشفرة"
                        VpnTab.SHARE_GUIDE -> "دليل مشاركة الاتصال والأجهزة"
                        VpnTab.SERVERS -> "الخوادم والمواقع العالمية"
                        VpnTab.SETTINGS -> "الأدوات وحماية الـ DNS"
                    },
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // Status Indicator Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isConnected) EmeraldSuccess.copy(alpha = 0.15f) else DarkCardElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isConnected) EmeraldSuccess.copy(alpha = 0.5f) else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) EmeraldGlow else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "نشط" else "متوقف",
                        color = if (isConnected) EmeraldGlow else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SafeShieldBottomBar(
    currentTab: VpnTab,
    onTabSelected: (VpnTab) -> Unit
) {
    NavigationBar(
        containerColor = DarkCard,
        contentColor = TextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("main_navigation_bar")
    ) {
        val items = listOf(
            Triple(VpnTab.HOME, "الرئيسية", Icons.Default.Shield),
            Triple(VpnTab.SHARE_GUIDE, "طرق الاتصال", Icons.Default.Share),
            Triple(VpnTab.SERVERS, "الخوادم", Icons.Default.Public),
            Triple(VpnTab.SETTINGS, "الأدوات", Icons.Default.Tune)
        )

        items.forEach { (tab, title, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkBg,
                    selectedTextColor = CyanGlow,
                    indicatorColor = CyanPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
