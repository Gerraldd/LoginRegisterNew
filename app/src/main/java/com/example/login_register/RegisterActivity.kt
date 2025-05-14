package com.example.login_register

import android.os.Bundle
import com.example.login_register.api.ApiClient
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.login_register.request.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    // View components
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.arahLoginButton)

        // Set click listener for register button
        registerButton.setOnClickListener {
            val full_name = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (full_name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(full_name, email, password)
        }

        loginButton.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Function to register user
    private fun registerUser(full_name: String, email: String, password: String) {
        val request = RegisterRequest(full_name ,email, password)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.register(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("username", full_name)
                        editor.putString("message", message)
                        editor.apply()

                        Toast.makeText(this@RegisterActivity, "Register sukses", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Register gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}