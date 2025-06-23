package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.grupo12.clubdeportivoapp.databinding.ActivityAddSocioBinding
import java.time.LocalDate

class AddSocioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddSocioBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        setupListeners()
        setupAliasAutomatico()
        setupAliasValidation()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegistrar.setOnClickListener {
            if (validateForm()) {
                if (registrarSocio()) {
                    Toast.makeText(this, "Socio registrado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Complete todos los campos correctamente", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setupAliasAutomatico() {
        binding.etDni.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.etAlias.text.isNullOrEmpty()) {
                    generarAliasAutomatico()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etDni.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etAlias.text.isNullOrEmpty()) {
                generarAliasAutomatico()
            }
        }
    }

    private fun generarAliasAutomatico() {
        val dni = binding.etDni.text.toString().trim()
        if (dni.isNotEmpty() && dni.matches(Regex("\\d+"))) {
            val aliasPropuesto = "S-${dni.takeLast(4)}"
            binding.etAlias.setText(aliasPropuesto)
            verificarDisponibilidadAlias(aliasPropuesto)
        }
    }

    private fun setupAliasValidation() {
        binding.etAlias.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val alias = s?.toString()?.trim()
                if (!alias.isNullOrEmpty()) {
                    verificarDisponibilidadAlias(alias)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun verificarDisponibilidadAlias(alias: String) {
        if (alias.length < 3) return

        val isAvailable = !dbHelper.existeAlias(alias)

        if (isAvailable) {
            binding.etAlias.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_check, 0
            )
            binding.etAlias.compoundDrawableTintList =
                ContextCompat.getColorStateList(this, R.color.success_color)
        } else {
            binding.etAlias.setCompoundDrawablesWithIntrinsicBounds(
                0, 0, R.drawable.ic_error, 0
            )
            binding.etAlias.compoundDrawableTintList =
                ContextCompat.getColorStateList(this, R.color.error_color)
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        binding.etNombre.error = null
        binding.etApellido.error = null
        binding.etDni.error = null
        binding.etTelefono.error = null
        binding.etAlias.error = null

        if (binding.etNombre.text.toString().isEmpty()) {
            binding.etNombre.error = "Nombre requerido"
            isValid = false
        }

        if (binding.etApellido.text.toString().isEmpty()) {
            binding.etApellido.error = "Apellido requerido"
            isValid = false
        }

        if (binding.etDni.text.toString().isEmpty()) {
            binding.etDni.error = "DNI requerido"
            isValid = false
        } else if (!binding.etDni.text.toString().matches(Regex("\\d+"))) {
            binding.etDni.error = "DNI debe contener solo números"
            isValid = false
        } else if (dbHelper.existeDni(binding.etDni.text.toString().trim())) {
            binding.etDni.error = "Este DNI ya está registrado"
            isValid = false
        }

        if (binding.etTelefono.text.toString().isEmpty()) {
            binding.etTelefono.error = "Teléfono requerido"
            isValid = false
        }

        if (binding.etAlias.text.toString().isEmpty()) {
            binding.etAlias.error = "Alias requerido"
            isValid = false
        } else if (dbHelper.existeAlias(binding.etAlias.text.toString().trim())) {
            binding.etAlias.error = "Este alias ya está en uso"
            isValid = false
        }

        return isValid
    }

    private fun registrarSocio(): Boolean {
        val nombre = binding.etNombre.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val dni = binding.etDni.text.toString().trim()
        val alias = binding.etAlias.text.toString().trim()

        return try {
            val resultado = dbHelper.insertarSocio("$nombre $apellido", alias, dni)

            if (resultado != -1L) {
                runOnUiThread {
                    Toast.makeText(this, "Socio registrado exitosamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PerfilSocioActivity::class.java).apply {
                        putExtra("dni_socio", dni)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error al registrar socio: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
            false
        }
    }
}