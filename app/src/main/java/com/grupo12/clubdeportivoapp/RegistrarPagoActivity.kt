package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.grupo12.clubdeportivoapp.databinding.ActivityRegistrarPagoBinding
import java.text.SimpleDateFormat
import java.util.*

class RegistrarPagoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrarPagoBinding
    private lateinit var dbHelper: DatabaseHelper
    private var dniSocio: String = ""
    private var montoBase: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        dniSocio = intent.getStringExtra("dni_socio") ?: run {
            finish()
            return
        }

        setupViews()
        cargarDatosSocio()
    }

    private fun cargarDatosSocio() {
        val socio = dbHelper.obtenerSocioPorDni(dniSocio) ?: run {
            finish()
            return
        }

        binding.tvNombre.text = socio.nombreCompleto
        binding.tvDni.text = "DNI: ${socio.dni}"
    }

    private fun setupViews() {
        // Configurar Spinners
        binding.spUbicacion.adapter = ArrayAdapter.createFromResource(
            this, R.array.ubicaciones, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spMetodoPago.adapter = ArrayAdapter.createFromResource(
            this, R.array.metodos_pago, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spFrecuencia.adapter = ArrayAdapter.createFromResource(
            this, R.array.frecuencias, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spMeses.adapter = ArrayAdapter.createFromResource(
            this, R.array.meses_pago, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Listeners
        binding.spUbicacion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                actualizarClasesDisponibles()
                calcularMonto()
                actualizarFechaVencimiento()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spFrecuencia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                calcularMonto()
                actualizarFechaVencimiento()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spMeses.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                calcularMonto()
                actualizarFechaVencimiento()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.spMetodoPago.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                calcularMonto()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.btnRegistrarPago.setOnClickListener {
            registrarPago()
        }

        binding.btnBack.setOnClickListener { finish() }

        // Calcular monto inicial
        calcularMonto()
        actualizarFechaVencimiento()
    }

    private fun actualizarClasesDisponibles() {
        val ubicacion = binding.spUbicacion.selectedItem.toString()

        if (ubicacion == "Salón de musculación") {
            binding.spClase.isEnabled = false
            binding.spClase.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf("Musculación")
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        } else {
            binding.spClase.isEnabled = true
            val arrayResId = when (ubicacion) {
                "Salón de clases" -> R.array.clases_salon
                else -> R.array.clases_exterior
            }

            binding.spClase.adapter = ArrayAdapter.createFromResource(
                this, arrayResId, android.R.layout.simple_spinner_item
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
    }

    private fun calcularMonto() {
        val ubicacion = binding.spUbicacion.selectedItem.toString()
        val frecuencia = binding.spFrecuencia.selectedItem.toString()
        val metodoPago = binding.spMetodoPago.selectedItem.toString()
        val mesesSeleccionados = binding.spMeses.selectedItem.toString().split(" ")[0].toInt()

        // Calcular monto base mensual según ubicación y frecuencia
        val montoMensual = when (ubicacion) {
            "Exterior" -> when (frecuencia) {
                "Por clase" -> 15.0
                "2 veces por semana" -> 30.0
                "3 veces por semana" -> 35.0
                else -> 40.0 // Pase libre
            }
            else -> when (frecuencia) { // Para salón de clases y musculación
                "Por clase" -> 10.0
                "2 veces por semana" -> 25.0
                "3 veces por semana" -> 30.0
                else -> 35.0 // Pase libre
            }
        }

        // Aplicar ajustes por método de pago al monto mensual
        val montoMensualFinal = when {
            metodoPago.contains("Mercado Pago") -> montoMensual * 1.05
            metodoPago.contains("Banco Provincia") -> montoMensual * 0.90
            else -> montoMensual
        }

        // Calcular monto total por los meses seleccionados
        montoBase = montoMensualFinal * mesesSeleccionados

        binding.tvMontoCalculado.text = "$${"%.2f".format(montoBase)} (${mesesSeleccionados} mes/es)"
    }

    private fun actualizarFechaVencimiento() {
        try {
            val mesesSeleccionados = binding.spMeses.selectedItem.toString().split(" ")[0].toInt()
            val fechaVencimiento = calcularVencimiento(mesesSeleccionados)
            binding.tvFechaVencimiento.text = "Vence: $fechaVencimiento"
        } catch (e: Exception) {
            binding.tvFechaVencimiento.text = "Vence: --/--/----"
        }
    }

    private fun calcularVencimiento(meses: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, meses)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun registrarPago() {
        val ubicacion = binding.spUbicacion.selectedItem.toString()
        val clase = if (ubicacion == "Salón de musculación") "Musculación"
        else binding.spClase.selectedItem.toString()
        val frecuencia = binding.spFrecuencia.selectedItem.toString()
        val metodoPago = binding.spMetodoPago.selectedItem.toString()
        val meses = binding.spMeses.selectedItem.toString().split(" ")[0].toInt()
        val fechaVencimiento = calcularVencimiento(meses)

        val success = dbHelper.registrarPago(
            dniSocio = dniSocio,
            monto = montoBase,
            tipoPago = metodoPago,
            actividad = "$ubicacion - $clase",
            frecuencia = "$frecuencia (${meses} mes/es)",
            mesesPagados = meses,
            fechaVencimiento = fechaVencimiento
        )

        if (success) {
            Toast.makeText(this, "Pago registrado exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al registrar pago", Toast.LENGTH_SHORT).show()
        }
    }
}