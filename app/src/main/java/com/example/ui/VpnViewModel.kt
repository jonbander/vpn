package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ConnectionStatus
import com.example.model.DiagnosticResult
import com.example.model.DnsProvider
import com.example.model.ProxyServerInfo
import com.example.model.ServerCategory
import com.example.model.SplitTunnelApp
import com.example.model.VpnProtocol
import com.example.model.VpnServer
import com.example.model.VpnStatistics
import com.example.vpn.SafeShieldVpnService
import com.example.vpn.VpnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VpnTab(val labelAr: String, val labelEn: String) {
    HOME("الرئيسية", "Home"),
    SHARE_GUIDE("طرق الاتصال", "Connect & Share"),
    SERVERS("الخوادم", "Servers"),
    SETTINGS("الأدوات", "Tools & Setup")
}

class VpnViewModel : ViewModel() {

    val connectionStatus: StateFlow<ConnectionStatus> = VpnRepository.connectionStatus
    val selectedServer: StateFlow<VpnServer> = VpnRepository.selectedServer
    val statistics: StateFlow<VpnStatistics> = VpnRepository.statistics
    val selectedDns: StateFlow<DnsProvider> = VpnRepository.selectedDns
    val customDnsIp: StateFlow<String> = VpnRepository.customDnsIp
    val selectedProtocol: StateFlow<VpnProtocol> = VpnRepository.selectedProtocol
    val killSwitch: StateFlow<Boolean> = VpnRepository.killSwitch
    val autoConnectOnWifi: StateFlow<Boolean> = VpnRepository.autoConnectOnWifi
    val proxyInfo: StateFlow<ProxyServerInfo> = VpnRepository.proxyInfo
    val splitTunnelApps: StateFlow<List<SplitTunnelApp>> = VpnRepository.splitTunnelApps
    val diagnosticResult: StateFlow<DiagnosticResult> = VpnRepository.diagnosticResult

    private val _currentTab = MutableStateFlow(VpnTab.HOME)
    val currentTab: StateFlow<VpnTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ServerCategory.ALL)
    val selectedCategory: StateFlow<ServerCategory> = _selectedCategory.asStateFlow()

    val filteredServers: StateFlow<List<VpnServer>> = combine(
        VpnRepository.servers,
        _searchQuery,
        _selectedCategory
    ) { servers, query, category ->
        servers.filter { server ->
            val matchesCategory = when (category) {
                ServerCategory.ALL -> true
                ServerCategory.FASTEST -> server.latencyMs <= 45
                ServerCategory.RECOMMENDED -> server.isRecommended
                ServerCategory.STREAMING -> server.streamingSupported
            }
            val matchesQuery = query.isEmpty() ||
                server.countryNameAr.contains(query, ignoreCase = true) ||
                server.countryNameEn.contains(query, ignoreCase = true) ||
                server.cityAr.contains(query, ignoreCase = true) ||
                server.cityEn.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VpnRepository.servers.value)

    fun setTab(tab: VpnTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: ServerCategory) {
        _selectedCategory.value = category
    }

    fun selectServer(server: VpnServer) {
        VpnRepository.selectServer(server)
    }

    fun toggleConnection(context: Context, onPrepareRequired: (Intent) -> Unit) {
        when (connectionStatus.value) {
            ConnectionStatus.CONNECTED, ConnectionStatus.CONNECTING -> {
                SafeShieldVpnService.stopVpn(context)
            }
            ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> {
                val prepareIntent = VpnService.prepare(context)
                if (prepareIntent != null) {
                    onPrepareRequired(prepareIntent)
                } else {
                    SafeShieldVpnService.startVpn(context)
                }
            }
            ConnectionStatus.DISCONNECTING -> {}
        }
    }

    fun onVpnPermissionGranted(context: Context) {
        SafeShieldVpnService.startVpn(context)
    }

    fun toggleProxy(context: Context, enable: Boolean, port: Int = 8080) {
        VpnRepository.toggleProxyServer(context, enable, port)
    }

    fun setDns(dns: DnsProvider, customIp: String = "") {
        VpnRepository.setDns(dns, customIp)
    }

    fun setProtocol(protocol: VpnProtocol) {
        VpnRepository.setProtocol(protocol)
    }

    fun toggleKillSwitch(enabled: Boolean) {
        VpnRepository.toggleKillSwitch(enabled)
    }

    fun toggleAutoConnect(enabled: Boolean) {
        VpnRepository.toggleAutoConnect(enabled)
    }

    fun loadApps(context: Context) {
        VpnRepository.loadInstalledApps(context)
    }

    fun toggleAppBypass(packageName: String) {
        VpnRepository.toggleAppBypass(packageName)
    }

    fun runDiagnostics() {
        VpnRepository.runDiagnostics()
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم النسخ إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
    }

    fun shareConfig(context: Context, title: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(intent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
