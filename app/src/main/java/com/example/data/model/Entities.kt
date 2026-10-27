package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val avatarColorIndex: Int = 0
)

@Entity(tableName = "files")
data class FileRecordEntity(
    @PrimaryKey
    val id: String, // UUID
    val fileName: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val localUri: String,
    val transferMode: String, // ORIGINAL, DATA_SAVER, SMART
    val sha256Checksum: String,
    val uploadedAt: Long = System.currentTimeMillis(),
    val ownerUsername: String,
    val category: String // MEDIA, DOC, ARCHIVE, AUDIO, OTHER
)

@Entity(tableName = "share_links")
data class ShareLinkEntity(
    @PrimaryKey
    val token: String, // e.g. "AY-8492X"
    val fileId: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val sha256Checksum: String,
    val transferMode: String,
    val pinHash: String = "", // SHA256 of PIN if protected
    val hasPin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = 0L, // 0 for Never, else millis timestamp
    val downloadCount: Int = 0,
    val maxDownloads: Int = 0, // 0 for Unlimited
    val isRevoked: Boolean = false,
    val ownerUsername: String = ""
)

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: String, // SNAKE, TICTACTOE, MEMORY, G2048, RPS
    val score: Int,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val username: String = "Guest"
)
