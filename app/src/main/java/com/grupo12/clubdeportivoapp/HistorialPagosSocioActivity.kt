package com.grupo12.clubdeportivoapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo12.clubdeportivoapp.databinding.ActivityHistorialPagosSocioBinding
import java.text.SimpleDateFormat
import java.util.*

class HistorialPagosSocioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialPagosSocioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialPagosSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView con datos de ejemplo
        val pagos = listOf(
            Pago("Socio Juan Pérez", "$100.00", "Efectivo", "2025-05-01"),
            Pago("Socio Ana Gómez", "$150.00", "Tarjeta", "2025-05-02"),
            Pago("Socio Carlos López", "$200.00", "Transferencia", "2025-05-03")
        )

        binding.rvHistorialPagos.layoutManager = LinearLayoutManager(this)
        binding.rvHistorialPagos.adapter = PagoAdapter(pagos)

        // Botón Volver
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Botón Descargar Reporte (mostrar modal)
        binding.btnDescargarReporte.setOnClickListener {
            showReporteGeneradoDialog()
        }
    }

    private fun showReporteGeneradoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reporte_generado, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Configurar nombre del archivo con fecha actual
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        dialogView.findViewById<TextView>(R.id.tv_nombre_archivo).text = "reporte_pagos-$currentDate.pdf"

        // Botón Cerrar
        dialogView.findViewById<Button>(R.id.btn_cerrar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}