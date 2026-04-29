package com.omnimap.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.omnimap.data.remote.api.GoogleAuthApi
import com.omnimap.domain.repository.AuthRepository
import com.omnimap.domain.repository.UserProfile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

class AndroidAuthRepository(private val context: Context) : AuthRepository {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser

    private val _authResults = MutableSharedFlow<Result<UserProfile>>()
    override val authResults: SharedFlow<Result<UserProfile>> = _authResults.asSharedFlow()

    private val clientId = "724915152342-9v4s9v4s9v4s9v4s.apps.googleusercontent.com" // Placeholder
    private val clientSecret = "GOCSPX-v4s9v4s9v4s9v4s9v4s" // Placeholder
    private val redirectUri = "com.omnimap://auth"

    private val authApi = Retrofit.Builder()
        .baseUrl("https://oauth2.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleAuthApi::class.java)

    override suspend fun login(): Result<UserProfile> {
        val state = UUID.randomUUID().toString()
        val authUrl = Uri.parse("https://accounts.google.com/o/oauth2/v2/auth")
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email")
            .appendQueryParameter("state", state)
            .build()
            .toString()
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)

        return authResults.first()
    }

    fun handleRedirect(uri: Uri) {
        val code = uri.getQueryParameter("code")
        CoroutineScope(Dispatchers.IO).launch {
            if (code != null) {
                try {
                    val tokenResponse = authApi.exchangeCodeForToken(
                        code = code,
                        clientId = clientId,
                        clientSecret = clientSecret,
                        redirectUri = redirectUri
                    )
                    val userInfo = authApi.getUserInfo("Bearer ${tokenResponse.access_token}")
                    val user = UserProfile(
                        id = userInfo.sub,
                        name = userInfo.name,
                        email = userInfo.email,
                        pictureUrl = userInfo.picture,
                        idToken = tokenResponse.id_token,
                        accessToken = tokenResponse.access_token
                    )
                    _currentUser.value = user
                    _authResults.emit(Result.success(user))
                } catch (e: Exception) {
                    _authResults.emit(Result.failure(e))
                }
            } else {
                _authResults.emit(Result.failure(Exception("No code found in redirect URI")))
            }
        }
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
