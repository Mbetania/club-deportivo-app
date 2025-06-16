package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lvCuotasAtrasadas.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            listOf("Juan Pérez - Vence: 15/05/2023", "María Gómez - Vence: 20/05/2023")
        )

        binding.btnRegistrarSocio.setOnClickListener {
            startActivity(Intent(this, AddSocioActivity::class.java))
        }

        binding.btnEditarSocio.setOnClickListener {
            startActivity(Intent(this, FindSocio::class.java))
        }

        binding.btnRegistrarPago.setOnClickListener {
            startActivity(Intent(this, FindSocio::class.java))
        }

        binding.btnReporteVencimientos.setOnClickListener {
            startActivity(Intent(this, ReporteVencimientosActivity::class.java))
        }

        binding.btnHistorialCobros.setOnClickListener {
            startActivity(Intent(this, HistorialPagosSocioActivity::class.java))
        }
    }
}