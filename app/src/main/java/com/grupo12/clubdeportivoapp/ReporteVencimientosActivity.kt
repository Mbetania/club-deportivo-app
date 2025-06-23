package com.grupo12.clubdeportivoapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.grupo12.clubdeportivoapp.databinding.ActivityReporteVencimientosBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReporteVencimientosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReporteVencimientosBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReporteVencimientosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupViews()
        cargarVencimientosProximos()
    }

    private fun cargarVencimientosProximos() {
        val socios = dbHelper.obtenerSociosConVencimientosProximos()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        val items = socios.map { socio ->
            "${socio.nombre} - Vence: ${socio.vencimiento.format(dateFormatter)}"
        }

        binding.rvUsuariosVencidos.layoutManager = LinearLayoutManager(this)
        binding.rvUsuariosVencidos.adapter = VencimientosAdapter(
            if (items.isEmpty()) listOf("No hay vencimientos próximos") else items
        )
    }

    private fun setupViews() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnDescargarReporte.setOnClickListener { showReporteGeneradoDialog() }
    }

    private fun showReporteGeneradoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reporte_generado, null)
        AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
            .apply {
                dialogView.findViewById<TextView>(R.id.tv_nombre_archivo).text =
                    "reporte_vencimientos-${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}.pdf"

                dialogView.findViewById<Button>(R.id.btn_cerrar).setOnClickListener { dismiss() }
                show()
            }
    }
}

class VencimientosAdapter(private val usuarios: List<String>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<VencimientosAdapter.ViewHolder>() {

    class ViewHolder(itemView: android.view.View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val textView: android.widget.TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = usuarios[position]
    }

    override fun getItemCount(): Int = usuarios.size
}