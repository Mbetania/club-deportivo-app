package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityRegistrarPagoBinding

class RegistrarPagoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarPagoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        val socio = intent.getSerializableExtra("socio") as? Socio ?: run {
            startActivity(Intent(this, FindSocio::class.java))
            finish()
            return
        }

        binding.tvNombre.text = socio.nombre
        binding.tvDni.text = "DNI: ${socio.dni}"

        binding.spTipoPago.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipos_pago,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnRegistrarPago.setOnClickListener {
            val monto = binding.etMonto.text.toString()
            if (monto.isEmpty()) {
                Toast.makeText(this, "Ingrese un monto válido", Toast.LENGTH_SHORT).show()
            } else {
                registrarPago(monto)
            }
        }
    }

    private fun registrarPago(monto: String) {
        val tipoPago = binding.spTipoPago.selectedItem.toString()
        Toast.makeText(this, "Pago registrado: $$monto ($tipoPago)", Toast.LENGTH_SHORT).show()
        finish()
    }
}