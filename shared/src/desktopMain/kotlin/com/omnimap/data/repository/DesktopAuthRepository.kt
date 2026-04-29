package com.omnimap.data.repository

import com.omnimap.core.util.BrowserUtil
import com.omnimap.data.remote.api.GoogleAuthApi
import com.omnimap.domain.repository.AuthRepository
import com.omnimap.domain.repository.UserProfile
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ServerSocket
import java.util.UUID

class DesktopAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    override val currentUser: StateFlow<UserProfile?> = _currentUser

    // These should ideally be injected or moved to a config
    private val clientId = "724915152342-9v4s9v4s9v4s9v4s.apps.googleusercontent.com" // Placeholder
    private val clientSecret = "GOCSPX-v4s9v4s9v4s9v4s9v4s" // Placeholder
    private val redirectPort = 8080
    private val redirectUri = "http://127.0.0.1:$redirectPort"

    private val authApi = Retrofit.Builder()
        .baseUrl("https://oauth2.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GoogleAuthApi::class.java)

    override suspend fun login(): Result<UserProfile> = withContext(Dispatchers.IO) {
        var serverSocket: ServerSocket? = null
        try {
            val state = UUID.randomUUID().toString()
            val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                    "client_id=$clientId&" +
                    "redirect_uri=$redirectUri&" +
                    "response_type=code&" +
                    "scope=https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email&" +
                    "state=$state"

            BrowserUtil.openUrl(authUrl)

            serverSocket = ServerSocket(redirectPort)
            val client = serverSocket.accept()
            val reader = client.getInputStream().bufferedReader()
            val firstLine = reader.readLine()
            
            val code = firstLine?.substringAfter("code=")?.substringBefore("&")?.substringBefore(" ")
            
            val responseBody = "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px;'>" +
                    "<h1>OmniMap Authentication Successful!</h1>" +
                    "<p>You can close this window and return to the app.</p>" +
                    "</body></html>"
            
            val response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: ${responseBody.length}\r\n" +
                    "Connection: close\r\n" +
                    "\r\n" +
                    responseBody
            
            client.getOutputStream().write(response.toByteArray())
            client.getOutputStream().flush()
            client.close()

            if (code == null) return@withContext Result.failure(Exception("Failed to get authorization code"))

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
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            serverSocket?.close()
        }
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
