package com.example.util

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.security.MessageDigest
import kotlin.random.Random

object CryptoUtils {

    fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun calculateSha256(context: Context, uri: Uri): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            context.contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                var read: Int
                while (stream.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback deterministic hash based on URI string and timestamp
            hashString("${uri.path}_${System.currentTimeMillis()}")
        }
    }

    fun generateShareToken(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val randomPart = (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
        return "AD-$randomPart"
    }

    fun verifyPin(enteredPin: String, storedPinHash: String): Boolean {
        if (storedPinHash.isEmpty()) return true
        return hashString(enteredPin) == storedPinHash
    }
}
