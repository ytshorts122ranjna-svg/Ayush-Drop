package com.example.data.repository

import com.example.data.dao.AyushDropDao
import com.example.data.model.UserEntity
import com.example.util.CryptoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class UserSession(
    val id: Long = 0,
    val username: String = "Guest",
    val email: String = "guest@ayushdrop.local",
    val isAuthenticated: Boolean = false,
    val storageQuotaBytes: Long = 1024L * 1024L * 1024L * 5L // 5 GB
)

class AuthRepository(private val dao: AyushDropDao) {

    private val _currentSession = MutableStateFlow(UserSession())
    val currentSession: StateFlow<UserSession> = _currentSession.asStateFlow()

    suspend fun signUp(username: String, email: String, password: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            val trimmedUser = username.trim()
            val trimmedEmail = email.trim()
            if (trimmedUser.length < 3) {
                return@withContext Result.failure(Exception("Username must be at least 3 characters."))
            }
            if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                return@withContext Result.failure(Exception("Please enter a valid email address."))
            }
            if (password.length < 6) {
                return@withContext Result.failure(Exception("Password must be at least 6 characters."))
            }

            if (dao.getUserByUsername(trimmedUser) != null) {
                return@withContext Result.failure(Exception("Username already exists."))
            }
            if (dao.getUserByEmail(trimmedEmail) != null) {
                return@withContext Result.failure(Exception("Email is already registered."))
            }

            val newUser = UserEntity(
                username = trimmedUser,
                email = trimmedEmail,
                passwordHash = CryptoUtils.hashString(password),
                createdAt = System.currentTimeMillis()
            )
            val id = dao.insertUser(newUser)
            val createdUser = newUser.copy(id = id)

            _currentSession.value = UserSession(
                id = id,
                username = createdUser.username,
                email = createdUser.email,
                isAuthenticated = true
            )

            Result.success(createdUser)
        }

    suspend fun signIn(emailOrUsername: String, password: String): Result<UserEntity> =
        withContext(Dispatchers.IO) {
            val trimmed = emailOrUsername.trim()
            val user = dao.getUserByUsername(trimmed) ?: dao.getUserByEmail(trimmed)
            if (user == null) {
                return@withContext Result.failure(Exception("User not found."))
            }

            val hash = CryptoUtils.hashString(password)
            if (user.passwordHash != hash) {
                return@withContext Result.failure(Exception("Incorrect password."))
            }

            _currentSession.value = UserSession(
                id = user.id,
                username = user.username,
                email = user.email,
                isAuthenticated = true
            )

            Result.success(user)
        }

    fun continueAsGuest() {
        _currentSession.value = UserSession(
            id = 0,
            username = "Guest Explorer",
            email = "guest@ayushdrop.local",
            isAuthenticated = false
        )
    }

    fun signOut() {
        _currentSession.value = UserSession()
    }
}
