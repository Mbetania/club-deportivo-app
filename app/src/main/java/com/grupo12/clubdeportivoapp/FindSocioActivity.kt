package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.grupo12.clubdeportivoapp.databinding.ActivityFindSocioBinding

class FindSocio : AppCompatActivity() {
    private lateinit var binding: ActivityFindSocioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupViews()
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }
    }

    private fun setupViews() {
        // Botón Buscar: Navega directamente a PerfilSocioActivity sin validaciones
        binding.btnBuscar.setOnClickListener {
            startActivity(Intent(this, PerfilSocioActivity::class.java))
        }

        // Botón Atrás: Cierra la actividad
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}