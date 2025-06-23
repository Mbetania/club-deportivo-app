package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityPerfilSocioBinding

class EditarSocioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilSocioBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var dniSocio: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        dniSocio = intent.getStringExtra("dni_socio") ?: run {
            Toast.makeText(this, "DNI no proporcionado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        cargarDatosSocio()
    }

    private fun cargarDatosSocio() {
        val socio = dbHelper.obtenerSocioPorDni(dniSocio) ?: run {
            Toast.makeText(this, "Socio no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        with(binding) {
            tvNombre.text = socio.nombreCompleto
            tvDni.text = "DNI: ${socio.dni}"
            // Agregar otros campos si existen en el layout
        }
    }

    private fun setupViews() {
        with(binding) {
            btnBack.setOnClickListener { finish() }

            btnRegistrarPago.setOnClickListener {
                navegarARegistrarPago()
            }

            // Configurar otros botones según necesidad
        }
    }

    private fun navegarARegistrarPago() {
        val intent = Intent(this, RegistrarPagoActivity::class.java).apply {
            putExtra("dni_socio", dniSocio)
            // Agregar flags para limpiar el stack si es necesario
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }
}