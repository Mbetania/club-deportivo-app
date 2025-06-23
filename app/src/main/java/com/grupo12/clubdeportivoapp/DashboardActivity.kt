package com.grupo12.clubdeportivoapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityDashboardBinding
import java.time.format.DateTimeFormatter

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        cargarCuotasAtrasadas()
        setupBotones()
    }

    private fun cargarCuotasAtrasadas() {
        val sociosVencidos = dbHelper.obtenerSociosConCuotasVencidas()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val items = sociosVencidos.map { socio ->
            "${socio.nombre} - Vence: ${socio.vencimiento.format(dateFormatter)}"
        }

        binding.lvCuotasAtrasadas.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            if (items.isEmpty()) listOf("No hay cuotas atrasadas") else items
        )
    }

    private fun setupBotones() {
        binding.apply {
            btnRegistrarSocio.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, AddSocioActivity::class.java))
            }

            btnEditarSocio.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, FindSocio::class.java))
            }

            btnRegistrarPago.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, FindSocio::class.java).apply {
                    putExtra("destino", "registrar_pago")
                })
            }

            btnReporteVencimientos.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, ReporteVencimientosActivity::class.java))
            }

            btnHistorialCobros.setOnClickListener {
                startActivity(Intent(this@DashboardActivity, FindSocio::class.java).apply {
                    putExtra("destino", "historial_cobros")
                })
            }

            btnLogout.setOnClickListener {
                cerrarSesion()
            }
        }
    }

    private fun cerrarSesion() {
        getSharedPreferences("Sesion", Context.MODE_PRIVATE).edit().clear().apply()

        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(this)
        }

        finish()
    }
}