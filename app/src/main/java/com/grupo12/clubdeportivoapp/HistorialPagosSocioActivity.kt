package com.grupo12.clubdeportivoapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import com.grupo12.clubdeportivoapp.databinding.ActivityHistorialPagosSocioBinding

class HistorialPagosSocioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialPagosSocioBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var dniSocio: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialPagosSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        dniSocio = intent.getStringExtra("dni_socio") ?: run {
            Toast.makeText(this, "DNI no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        cargarHistorialPagos()
    }

    private fun cargarHistorialPagos() {
        val pagos = dbHelper.obtenerHistorialPagos(dniSocio)

        if (pagos.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvHistorialPagos.visibility = View.GONE
            binding.btnDescargarReporte.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvHistorialPagos.visibility = View.VISIBLE
            binding.btnDescargarReporte.visibility = View.VISIBLE

            binding.rvHistorialPagos.layoutManager = LinearLayoutManager(this)
            binding.rvHistorialPagos.adapter = PagoAdapter(pagos)
        }
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnDescargarReporte.setOnClickListener {
            // Lógica para generar reporte PDF
            showReporteGeneradoDialog()
        }
    }

    private fun showReporteGeneradoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reporte_generado, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val currentDate = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            .format(java.util.Date())
        dialogView.findViewById<TextView>(R.id.tv_nombre_archivo).text =
            "reporte_pagos_${dniSocio}_$currentDate.pdf"

        dialogView.findViewById<Button>(R.id.btn_cerrar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}