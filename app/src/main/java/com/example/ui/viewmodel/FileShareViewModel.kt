package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FileRecordEntity
import com.example.data.model.ShareLinkEntity
import com.example.data.repository.FileRepository
import com.example.util.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingFileUpload(
    val uri: Uri,
    val fileName: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val category: String,
    val recommendation: FileUtils.ModeRecommendation
)

data class UploadProgressState(
    val isUploading: Boolean = false,
    val progressPercent: Float = 0f,
    val currentBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val statusMessage: String = ""
)

class FileShareViewModel(
    private val repository: FileRepository,
    private val context: Context
) : ViewModel() {

    // Pending file picked
    private val _pendingUpload = MutableStateFlow<PendingFileUpload?>(null)
    val pendingUpload: StateFlow<PendingFileUpload?> = _pendingUpload.asStateFlow()

    // Configured options for current upload
    val selectedMode = MutableStateFlow("ORIGINAL") // ORIGINAL, DATA_SAVER, SMART
    val pinProtection = MutableStateFlow("")
    val expirationHours = MutableStateFlow(24) // 1, 24, 168 (7 days), 0 (Never)

    // Upload progress
    private val _uploadState = MutableStateFlow(UploadProgressState())
    val uploadState: StateFlow<UploadProgressState> = _uploadState.asStateFlow()

    // Recently generated share link to display in dialog / created card
    private val _generatedLink = MutableStateFlow<ShareLinkEntity?>(null)
    val generatedLink: StateFlow<ShareLinkEntity?> = _generatedLink.asStateFlow()

    // Search and filter for Vault
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("ALL") // ALL, MEDIA, DOC, ARCHIVE, AUDIO

    // Recipient claim state
    private val _claimedFile = MutableStateFlow<ShareLinkEntity?>(null)
    val claimedFile: StateFlow<ShareLinkEntity?> = _claimedFile.asStateFlow()
    val claimError = MutableStateFlow<String?>(null)
    val isClaiming = MutableStateFlow(false)

    // Current user identifier for filtering files
    val currentUser = MutableStateFlow("Guest")

    // Reactive list of files in Vault
    val allFiles: StateFlow<List<FileRecordEntity>> = combine(
        repository.getFilesForUser("Guest"),
        searchQuery,
        selectedCategory
    ) { files, query, category ->
        files.filter { file ->
            val matchesQuery = query.isBlank() || file.fileName.contains(query, ignoreCase = true)
            val matchesCategory = category == "ALL" || file.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reactive list of share links
    val allShareLinks: StateFlow<List<ShareLinkEntity>> =
        repository.getShareLinksForUser("Guest")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total storage used
    val totalStorageUsed: StateFlow<Long> =
        repository.getTotalStorageUsed("Guest")
            .combine(MutableStateFlow(0L)) { used, _ -> used ?: 0L }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun onFileSelected(uri: Uri) {
        val (fileName, size) = FileUtils.getFileNameAndSize(context, uri)
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val category = FileUtils.detectCategory(fileName, mime)
        val rec = FileUtils.evaluateTransferMode(category, size)

        _pendingUpload.value = PendingFileUpload(
            uri = uri,
            fileName = fileName,
            fileSizeBytes = size,
            mimeType = mime,
            category = category,
            recommendation = rec
        )
        // Automatically default to recommendation
        selectedMode.value = rec.recommendedMode
    }

    fun clearPendingUpload() {
        _pendingUpload.value = null
        pinProtection.value = ""
        _generatedLink.value = null
        _uploadState.value = UploadProgressState()
    }

    fun startUploadAndGenerateLink(owner: String) {
        val pending = _pendingUpload.value ?: return

        viewModelScope.launch {
            _uploadState.value = UploadProgressState(
                isUploading = true,
                progressPercent = 0.05f,
                currentBytes = 0L,
                totalBytes = pending.fileSizeBytes,
                statusMessage = "Analyzing file integrity & computing SHA-256..."
            )

            // Real simulated byte transfer progress
            val totalBytes = pending.fileSizeBytes.coerceAtLeast(1024L)
            val steps = 10
            for (i in 1..steps) {
                delay(120)
                val current = (totalBytes * (i.toFloat() / steps)).toLong()
                val pct = (i.toFloat() / steps).coerceAtMost(0.95f)
                val msg = when {
                    i < 4 -> "Preparing transfer mode [${selectedMode.value}]..."
                    i < 8 -> "Packing payload into secure vault (${FileUtils.formatBytes(current)})..."
                    else -> "Generating cryptographic token and deep link..."
                }
                _uploadState.value = UploadProgressState(
                    isUploading = true,
                    progressPercent = pct,
                    currentBytes = current,
                    totalBytes = totalBytes,
                    statusMessage = msg
                )
            }

            try {
                val link = repository.processAndSaveFile(
                    uri = pending.uri,
                    transferMode = selectedMode.value,
                    ownerUsername = owner,
                    pin = pinProtection.value,
                    expirationHours = expirationHours.value
                )

                _uploadState.value = UploadProgressState(
                    isUploading = false,
                    progressPercent = 1f,
                    currentBytes = totalBytes,
                    totalBytes = totalBytes,
                    statusMessage = "Transfer package ready!"
                )
                _generatedLink.value = link
            } catch (e: Exception) {
                _uploadState.value = UploadProgressState(
                    isUploading = false,
                    statusMessage = "Error: ${e.message}"
                )
            }
        }
    }

    fun claimLink(token: String, enteredPin: String) {
        viewModelScope.launch {
            isClaiming.value = true
            claimError.value = null
            delay(400) // Brief simulation of network handshake

            val result = repository.verifyAndClaimDownload(token.trim(), enteredPin.trim())
            result.onSuccess { link ->
                _claimedFile.value = link
                isClaiming.value = false
            }.onFailure { err ->
                claimError.value = err.message ?: "Failed to verify share link."
                isClaiming.value = false
            }
        }
    }

    fun clearClaimedFile() {
        _claimedFile.value = null
        claimError.value = null
    }

    fun revokeLink(token: String) {
        viewModelScope.launch {
            repository.revokeLink(token)
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteFileAndLinks(fileId)
        }
    }
}
