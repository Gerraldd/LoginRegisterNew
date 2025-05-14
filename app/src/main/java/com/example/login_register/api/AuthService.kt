package com.example.login_register.api

import com.example.login_register.request.LoginRequest
import com.example.login_register.request.RefreshTokenRequest
import com.example.login_register.request.RegisterRequest
import com.example.login_register.response.LoginResponse
import com.example.login_register.response.RefreshTokenResponse
import com.example.login_register.response.RegisterResponse
import com.example.login_register.response.getUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("token-refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @GET("users/113")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): Response<getUser>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Any>
}