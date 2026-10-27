package com.example.data.repository

import com.example.data.dao.AyushDropDao
import com.example.data.model.GameScoreEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ArcadeRepository(private val dao: AyushDropDao) {

    fun getLeaderboard(gameId: String): Flow<List<GameScoreEntity>> =
        dao.getLeaderboardForGame(gameId)

    fun getBestScore(gameId: String): Flow<Int?> =
        dao.getBestScoreForGame(gameId)

    fun getRecentGames(): Flow<List<GameScoreEntity>> =
        dao.getRecentGamesFlow()

    fun getTotalGamesPlayed(username: String): Flow<Int> =
        dao.getTotalGamesPlayedByUser(username)

    suspend fun saveGameScore(
        gameId: String,
        score: Int,
        details: String,
        username: String
    ) = withContext(Dispatchers.IO) {
        val entity = GameScoreEntity(
            gameId = gameId,
            score = score,
            details = details,
            timestamp = System.currentTimeMillis(),
            username = if (username.isBlank()) "Player" else username
        )
        dao.insertGameScore(entity)
    }
}
