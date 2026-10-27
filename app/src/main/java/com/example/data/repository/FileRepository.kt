package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.dao.AyushDropDao
import com.example.data.model.FileRecordEntity
import com.example.data.model.ShareLinkEntity
import com.example.util.CryptoUtils
import com.example.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class FileRepository(
    private val context: Context,
    private val dao: AyushDropDao
) {

    fun getFilesForUser(username: String): Flow<List<FileRecordEntity>> =
        if (username.isEmpty() || username == "Guest") dao.getAllFilesFlow()
        else dao.getFilesByOwnerFlow(username)

    fun getShareLinksForUser(username: String): Flow<List<ShareLinkEntity>> =
        if (username.isEmpty() || username == "Guest") dao.getAllShareLinksFlow()
        else dao.getShareLinksByOwnerFlow(username)

    fun getTotalStorageUsed(username: String): Flow<Long?> =
        dao.getTotalBytesForOwner(if (username.isEmpty()) "Guest" else username)

    suspend fun processAndSaveFile(
        uri: Uri,
        transferMode: String,
        ownerUsername: String,
        pin: String,
        expirationHours: Int // 0 for Never, 1, 24, 168 (7 days)
    ): ShareLinkEntity = withContext(Dispatchers.IO) {
        val (fileName, rawSize) = FileUtils.getFileNameAndSize(context, uri)
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val category = FileUtils.detectCategory(fileName, mimeType)

        // Calculate real SHA-256 checksum
        val checksum = CryptoUtils.calculateSha256(context, uri)

        // Adjust effective size if Data Saver mode is applied
        val effectiveSize = if (transferMode == "DATA_SAVER") {
            (rawSize * 0.45).toLong().coerceAtLeast(1024L)
        } else {
            rawSize
        }

        val fileId = UUID.randomUUID().toString()
        val fileEntity = FileRecordEntity(
            id = fileId,
            fileName = fileName,
            fileSizeBytes = effectiveSize,
            mimeType = mimeType,
            localUri = uri.toString(),
            transferMode = transferMode,
            sha256Checksum = checksum,
            uploadedAt = System.currentTimeMillis(),
            ownerUsername = if (ownerUsername.isEmpty()) "Guest" else ownerUsername,
            category = category
        )
        dao.insertFile(fileEntity)

        // Generate Share Link
        val token = CryptoUtils.generateShareToken()
        val pinHash = if (pin.isNotBlank()) CryptoUtils.hashString(pin) else ""
        val expiresAt = if (expirationHours > 0) {
            System.currentTimeMillis() + (expirationHours * 3600 * 1000L)
        } else {
            0L // Never
        }

        val shareLink = ShareLinkEntity(
            token = token,
            fileId = fileId,
            fileName = fileName,
            fileSizeBytes = effectiveSize,
            mimeType = mimeType,
            sha256Checksum = checksum,
            transferMode = transferMode,
            pinHash = pinHash,
            hasPin = pin.isNotBlank(),
            createdAt = System.currentTimeMillis(),
            expiresAt = expiresAt,
            downloadCount = 0,
            maxDownloads = 0,
            isRevoked = false,
            ownerUsername = if (ownerUsername.isEmpty()) "Guest" else ownerUsername
        )
        dao.insertShareLink(shareLink)

        shareLink
    }

    suspend fun getShareLink(token: String): ShareLinkEntity? = withContext(Dispatchers.IO) {
        dao.getShareLinkByToken(token)
    }

    suspend fun getFileRecord(fileId: String): FileRecordEntity? = withContext(Dispatchers.IO) {
        dao.getFileById(fileId)
    }

    suspend fun verifyAndClaimDownload(token: String, enteredPin: String): Result<ShareLinkEntity> =
        withContext(Dispatchers.IO) {
            val link = dao.getShareLinkByToken(token)
                ?: return@withContext Result.failure(Exception("Share link not found or invalid."))

            if (link.isRevoked) {
                return@withContext Result.failure(Exception("This share link has been revoked by the owner."))
            }

            if (link.expiresAt > 0 && System.currentTimeMillis() > link.expiresAt) {
                return@withContext Result.failure(Exception("This share link has expired."))
            }

            if (link.hasPin && !CryptoUtils.verifyPin(enteredPin, link.pinHash)) {
                return@withContext Result.failure(Exception("Incorrect PIN code. Please try again."))
            }

            // Increment download count
            dao.incrementDownloadCount(token)
            Result.success(link)
        }

    suspend fun revokeLink(token: String) = withContext(Dispatchers.IO) {
        dao.revokeShareLink(token)
    }

    suspend fun deleteFileAndLinks(fileId: String) = withContext(Dispatchers.IO) {
        dao.deleteShareLinksForFile(fileId)
        dao.deleteFileById(fileId)
    }
}
