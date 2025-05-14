package com.example.login_register.response

data class LoginResponse(
    val status: Int,
    val message: String,
    val data: LoginData,
    val accessToken: String,
    val refreshToken: String
)

data class LoginData(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String
)

data class RegisterResponse(
    val message: String
)

data class RefreshTokenResponse(
    val message: String,
    val accessToken: String
)

data class getUser(
    val status: Int,
    val message: String,
    val data: DataUser
)

data class DataUser(
    val id: Int,
    val full_name: String,
    val email: String,
    val role: String
)