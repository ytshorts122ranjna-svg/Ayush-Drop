package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.CryptoUtils
import com.example.util.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AyushDrop", appName)
  }

  @Test
  fun `crypto utils test token generation and pin hashing`() {
    val token = CryptoUtils.generateShareToken()
    assertTrue(token.startsWith("AD-"))
    assertEquals(9, token.length) // AD-XXXXXX

    val hash1 = CryptoUtils.hashString("1234")
    val hash2 = CryptoUtils.hashString("1234")
    assertEquals(hash1, hash2)
    assertTrue(CryptoUtils.verifyPin("1234", hash1))
  }

  @Test
  fun `file utils format bytes and category detection`() {
    assertEquals("1 KB", FileUtils.formatBytes(1024L))
    assertEquals("1 MB", FileUtils.formatBytes(1024L * 1024L))
    assertEquals("MEDIA", FileUtils.detectCategory("photo.png", "image/png"))
    assertEquals("DOC", FileUtils.detectCategory("report.pdf", "application/pdf"))
    assertEquals("ARCHIVE", FileUtils.detectCategory("backup.zip", "application/zip"))
  }
}
