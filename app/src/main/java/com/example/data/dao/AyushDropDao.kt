package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FileRecordEntity
import com.example.data.model.GameScoreEntity
import com.example.data.model.ShareLinkEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AyushDropDao {

    // --- Users ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    // --- Files ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileRecordEntity)

    @Query("SELECT * FROM files WHERE ownerUsername = :owner ORDER BY uploadedAt DESC")
    fun getFilesByOwnerFlow(owner: String): Flow<List<FileRecordEntity>>

    @Query("SELECT * FROM files ORDER BY uploadedAt DESC")
    fun getAllFilesFlow(): Flow<List<FileRecordEntity>>

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): FileRecordEntity?

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFileById(id: String)

    @Query("SELECT SUM(fileSizeBytes) FROM files WHERE ownerUsername = :owner")
    fun getTotalBytesForOwner(owner: String): Flow<Long?>

    // --- Share Links ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareLink(link: ShareLinkEntity)

    @Query("SELECT * FROM share_links WHERE token = :token LIMIT 1")
    suspend fun getShareLinkByToken(token: String): ShareLinkEntity?

    @Query("SELECT * FROM share_links WHERE ownerUsername = :owner ORDER BY createdAt DESC")
    fun getShareLinksByOwnerFlow(owner: String): Flow<List<ShareLinkEntity>>

    @Query("SELECT * FROM share_links ORDER BY createdAt DESC")
    fun getAllShareLinksFlow(): Flow<List<ShareLinkEntity>>

    @Query("UPDATE share_links SET downloadCount = downloadCount + 1 WHERE token = :token")
    suspend fun incrementDownloadCount(token: String)

    @Query("UPDATE share_links SET isRevoked = 1 WHERE token = :token")
    suspend fun revokeShareLink(token: String)

    @Query("DELETE FROM share_links WHERE token = :token")
    suspend fun deleteShareLink(token: String)

    @Query("DELETE FROM share_links WHERE fileId = :fileId")
    suspend fun deleteShareLinksForFile(fileId: String)

    // --- Arcade Scores ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameScore(score: GameScoreEntity)

    @Query("SELECT * FROM game_scores WHERE gameId = :gameId ORDER BY score DESC LIMIT 10")
    fun getLeaderboardForGame(gameId: String): Flow<List<GameScoreEntity>>

    @Query("SELECT MAX(score) FROM game_scores WHERE gameId = :gameId")
    fun getBestScoreForGame(gameId: String): Flow<Int?>

    @Query("SELECT * FROM game_scores ORDER BY timestamp DESC LIMIT 20")
    fun getRecentGamesFlow(): Flow<List<GameScoreEntity>>

    @Query("SELECT COUNT(*) FROM game_scores WHERE username = :username")
    fun getTotalGamesPlayedByUser(username: String): Flow<Int>
}
