package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.grupo12.clubdeportivoapp.databinding.ActivityLoginBinding
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.database.Cursor
import androidx.core.content.ContextCompat

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val dbHelper = DatabaseHelper(this)
            dbHelper.crearUsuarioAdmin()
            val db = dbHelper.writableDatabase // Fuerza la creación de la DB
            Log.d("DB_INIT", "Base de datos creada en: ${db.path}")

            // Verificación adicional (opcional)
            val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
            Log.d("DB_INIT", "Tablas existentes: ${cursor.count}")
            cursor.close()
        } catch (e: Exception) {
            Log.e("DB_INIT", "Error al inicializar DB", e)
            Toast.makeText(this, "Error al iniciar la base de datos", Toast.LENGTH_LONG).show()
        }

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }

        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            if (validateCredentials()) {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            } else {
                binding.tvError.apply {
                    text = "Credenciales incorrectas"
                    visibility = View.VISIBLE // Ahora reconocerá View
                }
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateCredentials(): Boolean {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        val dbHelper = DatabaseHelper(this)
        var isValid = false
        var cursor: Cursor? = null

        try {
            cursor = dbHelper.readableDatabase.rawQuery("""
            SELECT * FROM ${DatabaseHelper.TABLE_USUARIOS} 
            WHERE ${DatabaseHelper.COLUMN_USERNAME} = ? 
            AND ${DatabaseHelper.COLUMN_PASSWORD} = ?
        """, arrayOf(username, password))

            isValid = cursor?.count ?: 0 > 0
        } catch (e: Exception) {
            Log.e("LOGIN", "Error en BD", e)
        } finally {
            cursor?.close()
            dbHelper.close()
        }
        return isValid
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

}