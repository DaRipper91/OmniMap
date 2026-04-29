package com.omnimap.domain.repository

import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val pictureUrl: String?,
    val idToken: String? = null,
    val accessToken: String? = null
)

interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>
    suspend fun login(): Result<UserProfile>
    suspend fun logout()
}
