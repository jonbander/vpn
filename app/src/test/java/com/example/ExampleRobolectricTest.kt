package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DnsProvider
import com.example.vpn.NetworkUtils
import com.example.vpn.VpnRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SafeShield VPN", appName)
  }

  @Test
  fun `verify vpn servers catalog and config generator`() {
    val servers = VpnRepository.servers.value
    assertTrue("Servers catalog should not be empty", servers.isNotEmpty())

    val firstServer = servers.first()
    assertNotNull(firstServer.countryNameAr)
    assertNotNull(firstServer.ipAddress)

    val wireGuardConfig = NetworkUtils.generateWireGuardConfig(firstServer, DnsProvider.CLOUDFLARE)
    assertTrue("Config should contain Interface section", wireGuardConfig.contains("[Interface]"))
    assertTrue("Config should contain Peer section", wireGuardConfig.contains("[Peer]"))
    assertTrue("Config should contain endpoint IP", wireGuardConfig.contains(firstServer.ipAddress))
  }
}
