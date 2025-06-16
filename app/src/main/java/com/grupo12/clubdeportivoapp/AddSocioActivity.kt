package com.grupo12.clubdeportivoapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityAddSocioBinding

class AddSocioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddSocioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegistrar.setOnClickListener {
            if (validateForm()) {
                Toast.makeText(this, "Socio registrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateForm(): Boolean {
        return binding.etNombre.text.toString().isNotEmpty() &&
                binding.etApellido.text.toString().isNotEmpty() &&
                binding.etDni.text.toString().isNotEmpty() &&
                binding.etTelefono.text.toString().isNotEmpty()
    }
}