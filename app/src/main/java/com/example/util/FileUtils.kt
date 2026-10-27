package com.example.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.text.DecimalFormat

object FileUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return "${DecimalFormat("#,##0.#").format(value)} ${units[index]}"
    }

    fun detectCategory(fileName: String, mimeType: String): String {
        val lower = fileName.lowercase()
        return when {
            mimeType.startsWith("image/") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".heic") -> "MEDIA"
            mimeType.startsWith("video/") || lower.endsWith(".mp4") || lower.endsWith(".mkv") ||
                    lower.endsWith(".mov") || lower.endsWith(".avi") -> "MEDIA"
            mimeType.startsWith("audio/") || lower.endsWith(".mp3") || lower.endsWith(".m4a") ||
                    lower.endsWith(".wav") || lower.endsWith(".flac") -> "AUDIO"
            mimeType.contains("pdf") || lower.endsWith(".pdf") -> "DOC"
            mimeType.contains("word") || mimeType.contains("officedocument") ||
                    lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".txt") ||
                    lower.endsWith(".xlsx") || lower.endsWith(".pptx") || lower.endsWith(".csv") -> "DOC"
            mimeType.contains("zip") || mimeType.contains("tar") || mimeType.contains("compressed") ||
                    lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z") || lower.endsWith(".tar.gz") -> "ARCHIVE"
            else -> "OTHER"
        }
    }

    fun getFileNameAndSize(context: Context, uri: Uri): Pair<String, Long> {
        var name = "file_${System.currentTimeMillis()}"
        var size = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {
            // fallback
        }
        return Pair(name, size)
    }

    data class ModeRecommendation(
        val recommendedMode: String, // "ORIGINAL", "DATA_SAVER", "SMART"
        val estimatedSavingsPercent: Int,
        val rationale: String
    )

    fun evaluateTransferMode(category: String, sizeBytes: Long): ModeRecommendation {
        return when (category) {
            "MEDIA" -> {
                if (sizeBytes > 10 * 1024 * 1024) { // > 10MB
                    ModeRecommendation(
                        recommendedMode = "DATA_SAVER",
                        estimatedSavingsPercent = 65,
                        rationale = "Large media detected. Data Saver can reduce bandwidth by ~65% with balanced optimization."
                    )
                } else {
                    ModeRecommendation(
                        recommendedMode = "ORIGINAL",
                        estimatedSavingsPercent = 0,
                        rationale = "Compact media. Original Quality preserves every pixel without data overhead."
                    )
                }
            }
            "DOC" -> {
                ModeRecommendation(
                    recommendedMode = "ORIGINAL",
                    estimatedSavingsPercent = 0,
                    rationale = "Documents contain exact formatting. Original Quality ensures perfect readability."
                )
            }
            "ARCHIVE" -> {
                ModeRecommendation(
                    recommendedMode = "ORIGINAL",
                    estimatedSavingsPercent = 0,
                    rationale = "Archives are already compressed. Preserving original checksum ensures zero corruption."
                )
            }
            else -> {
                ModeRecommendation(
                    recommendedMode = "SMART",
                    estimatedSavingsPercent = 30,
                    rationale = "Smart Mode auto-adjusts packaging based on network conditions."
                )
            }
        }
    }
}
