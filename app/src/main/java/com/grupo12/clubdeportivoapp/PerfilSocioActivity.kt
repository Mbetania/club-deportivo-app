package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityPerfilSocioBinding

class PerfilSocioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPerfilSocioBinding
    private val dbHelper: DatabaseHelper by lazy { DatabaseHelper(this) }
    private lateinit var socio: Socio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dniSocio = intent.getStringExtra("dni_socio") ?: run {
            Toast.makeText(this, "DNI no proporcionado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val socio = dbHelper.obtenerSocioPorDni(dniSocio) ?: run {
            Toast.makeText(this, "Socio no encontrado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        with(binding) {
            tvNombre.text = socio.nombreCompleto
            tvDni.text = "DNI: ${socio.dni}"
            // Puedes agregar más campos aquí según necesites

            btnBack.setOnClickListener { finish() }

            btnRegistrarPago.setOnClickListener {
                startActivity(Intent(this@PerfilSocioActivity, RegistrarPagoActivity::class.java).apply {
                    putExtra("dni_socio", socio.dni)
                })
            }
        }
    }

    private fun showDatosGuardadosDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_guardar_ok, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<android.widget.Button>(R.id.btn_cerrar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}