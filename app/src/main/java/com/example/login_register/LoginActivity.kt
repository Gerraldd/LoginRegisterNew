package com.example.login_register

import android.os.Bundle
import com.example.login_register.api.ApiClient
import com.example.login_register.request.LoginRequest
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // View components
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Set click listener for register button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
    }

    // Function to register user
    private fun loginUser(email: String, password: String) {
        val request = LoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.login(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message
                        val full_name = it.data.full_name
                        val role = it.data.role
                        val accessToken = it.accessToken
                        val refreshToken = it.refreshToken

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("username", full_name)
                        editor.putString("message", message)
                        editor.putString("accessToken", accessToken)
                        editor.putString("refreshToken", refreshToken)
                        editor.apply()


                        Toast.makeText(this@LoginActivity, "Login sukses", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}