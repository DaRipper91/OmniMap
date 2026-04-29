package com.omnimap.data.remote.api

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface GoogleAuthApi {
    @FormUrlEncoded
    @POST("https://oauth2.googleapis.com/token")
    suspend fun exchangeCodeForToken(
        @Field("code") code: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): TokenResponse

    @GET("https://www.googleapis.com/oauth2/v3/userinfo")
    suspend fun getUserInfo(
        @Header("Authorization") authHeader: String
    ): GoogleUserInfo
}

data class TokenResponse(
    val access_token: String,
    val id_token: String,
    val expires_in: Int,
    val token_type: String,
    val refresh_token: String?
)

data class GoogleUserInfo(
    val sub: String,
    val name: String,
    val given_name: String,
    val family_name: String,
    val picture: String,
    val email: String,
    val email_verified: Boolean,
    val locale: String
)
