package com.sh.an.tsd.data.api

import com.sh.an.tsd.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApiService {
    
    @POST("api/v1/oauth/register")
    suspend fun registerOAuthClient(
        @Body clientData: OAuthClientCreate
    ): Response<OAuthClientResponse>
    
    @FormUrlEncoded
    @POST("api/v1/oauth/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String? = null,
        @Field("username") username: String? = null,
        @Field("password") password: String? = null,
        @Field("scope") scope: String = "read write"
    ): Response<AuthResponse>
    
    @GET("api/v1/oauth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<User>
    
    @GET("api/v1/oauth/client-info")
    suspend fun getClientInfo(
        @Header("Authorization") authorization: String
    ): Response<OAuthClient>
}

