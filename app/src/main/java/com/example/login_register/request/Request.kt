package com.example.login_register.request

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val full_name: String,
    val email: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)