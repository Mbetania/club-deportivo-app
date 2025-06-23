package com.grupo12.clubdeportivoapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.grupo12.clubdeportivoapp.databinding.ActivityFindSocioBinding

class FindSocio : AppCompatActivity() {
    private lateinit var binding: ActivityFindSocioBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: ArrayAdapter<String>
    private var destino: String? = null // Declaramos la variable destino

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindSocioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        destino = intent.getStringExtra("destino")
        dbHelper = DatabaseHelper(this)
        setupAdapter()
        setupWindowInsets()
        setupViews()
    }

    private fun setupAdapter() {
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        binding.lvResultados.adapter = adapter

        binding.lvResultados.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            selectedItem?.let {
                val dni = it.substringAfterLast("DNI: ").trim()
                if (dni.isNotEmpty()) {
                    handleSocioSelection(dni)
                }
            }
        }
    }

    private fun handleSocioSelection(dni: String) {
        when (destino) {
            "registrar_pago" -> {
                startActivity(Intent(this, RegistrarPagoActivity::class.java).apply {
                    putExtra("dni_socio", dni)
                })
            }
            "historial_cobros" -> {
                mostrarInformacionSocio(dni)
            }
            else -> {
                startActivity(Intent(this, PerfilSocioActivity::class.java).apply {
                    putExtra("dni_socio", dni)
                })
            }
        }
    }

    private fun mostrarInformacionSocio(dni: String) {
        val socio = dbHelper.obtenerSocioPorDni(dni)
        val pagos = dbHelper.obtenerHistorialPagos(dni)

        if (socio != null) {
            // Obtener información del último pago
            val ultimoPago = if (pagos.isNotEmpty()) pagos[0] else null

            val mensaje = buildString {
                append("DNI: ${socio.dni}\n")
                append("Número de Socio: ${socio.alias}\n")
                append("Nombre: ${socio.nombre} ${socio.apellido}\n")

                ultimoPago?.let {
                    append("Actividad: ${it.actividad}\n")
                    append("Frecuencia: ${it.frecuencia}\n")
                    // Asumimos mesesPagados como 1 si no está disponible
                    append("Meses pagados: ${1}\n")
                } ?: append("No hay información de pago reciente\n")

                append("Próximo vencimiento: ${socio.vencimiento}")
            }

            AlertDialog.Builder(this)
                .setTitle("Información del Socio")
                .setMessage(mensaje)
                .setPositiveButton("Ver Historial Completo") { _, _ ->
                    startActivity(Intent(this, HistorialPagosSocioActivity::class.java).apply {
                        putExtra("dni_socio", dni)
                    })
                }
                .setNegativeButton("Cerrar", null)
                .show()
        } else {
            Toast.makeText(this, "No se encontró el socio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }
    }

    private fun setupViews() {
        binding.btnBuscar.setOnClickListener {
            val query = binding.etBusqueda.text.toString().trim()
            if (query.isNotEmpty()) {
                buscarSocios(query)
            } else {
                Toast.makeText(this, "Ingrese un DNI para buscar", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.etBusqueda.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val query = it.toString().trim()
                    if (query.length >= 2) {
                        buscarSocios(query)
                    } else {
                        adapter.clear()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun buscarSocios(query: String) {
        val socios = if (query.matches(Regex("\\d+"))) {
            dbHelper.buscarSociosPorDni(query)
        } else {
            emptyList()
        }

        adapter.clear()
        if (socios.isNotEmpty()) {
            socios.forEach { socio ->
                adapter.add("${socio.nombre} ${socio.apellido} - DNI: ${socio.dni}")
            }
        } else {
            adapter.add("No se encontraron socios")
        }
    }
}