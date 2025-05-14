package com.example.login_register

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import android.util.Base64
import com.example.login_register.api.ApiClient
import com.example.login_register.request.RefreshTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var logoutButton: Button
    private lateinit var toolbar: Toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var tvId: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView

    private val handler = Handler(Looper.getMainLooper())

    // Interval pengecekan token dalam milidetik (5 detik)
    private val checkTokenInterval: Long = TimeUnit.SECONDS.toMillis(5)

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
                val jsonObject = JSONObject(payload)
                val expTime = jsonObject.getLong("exp") * 1000
                System.currentTimeMillis() > expTime
            } else true
        } catch (e: Exception) {
            true
        }
    }

    private fun startTokenChecker() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkAndRefreshToken()
                handler.postDelayed(this, checkTokenInterval)
            }
        }, checkTokenInterval)
    }

    private fun checkAndRefreshToken() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")
        val refreshToken = sharedPreferences.getString("refreshToken", "")

        if (accessToken != null && isTokenExpired(accessToken)) {
            if (refreshToken != null) {
                if (isTokenExpired(refreshToken)) {
                    logoutAndRedirectToLogin()
                } else {
                    refreshAccessToken(refreshToken)
                }
            } else {
                logoutAndRedirectToLogin()
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        val request = RefreshTokenRequest(refreshToken)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.refreshToken(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message
                        val newAccessToken = it.accessToken

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("message", message)
                        editor.putString("accessToken", newAccessToken)
                        editor.apply()
                    }
                } else {
                    logoutAndRedirectToLogin()
                    Toast.makeText(this@MainActivity, "Refresh token gagal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logoutAndRedirectToLogin() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")
        val message = "Logout Berhasil"

        CoroutineScope(Dispatchers.IO).launch {
            var logoutMessage = "Logout gagal"

            if (accessToken != null) {
                try {
                    val response = ApiClient.authService.logout("Bearer $accessToken")
                    logoutMessage = if (response.isSuccessful) {
                        message ?: "Logout Berhasil"
                    } else {
                        "Logout gagal di server"
                    }
                } catch (e: Exception) {
                    logoutMessage = "Terjadi kesalahan jaringan saat logout"
                }
            } else {
                logoutMessage = "Token tidak ditemukan, langsung logout"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, logoutMessage, Toast.LENGTH_SHORT).show()

                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                handler.removeCallbacksAndMessages(null)
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun getDataUser() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.getUser("Bearer $accessToken")

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val id = it.data.id
                        val fullname = it.data.full_name
                        val email = it.data.email
                        val role = it.data.role

                        tvId.text = "Id = $id"
                        tvFullName.text = "Full Name = $fullname"
                        tvEmail.text = "Email = $email"
                        tvRole.text = "Role = $role"
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal mendapatkan User", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcomeText = findViewById(R.id.welcomeText)
        logoutButton = findViewById(R.id.logoutButton)
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.main)
        navView = findViewById(R.id.navView)
        tvId = findViewById(R.id.tvId)
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        tvRole = findViewById(R.id.tvRole)

        // Set toolbar sebagai action bar
        setSupportActionBar(toolbar)

        // Aktifkan icon hamburger dan atur listener-nya
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_hamburger)

        // Saat tombol hamburger ditekan
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Ambil username dan role dari SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val full_name = sharedPreferences.getString("username", "")
        val role = sharedPreferences.getString("role", "")

        // Jika username ada, tampilkan pada TextView
        if (!full_name.isNullOrEmpty()) {
            welcomeText.text = "Selamat datang, $full_name!"
            welcomeText.visibility = View.VISIBLE
        }

        if (role == "member"){
            toolbar.visibility = View.GONE
        }

        // handle click menu
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> Toast.makeText(this, "Beranda dipilih", Toast.LENGTH_SHORT).show()
                R.id.nav_profile -> Toast.makeText(this, "Profil dipilih", Toast.LENGTH_SHORT).show()
                R.id.nav_settings -> Toast.makeText(this, "Pengaturan dipilih", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Logout button action
        logoutButton.setOnClickListener {
            logoutAndRedirectToLogin()
        }

        // Panggil fungsi untuk mulai pengecekan token periodik
        startTokenChecker()
        getDataUser()
    }
}
