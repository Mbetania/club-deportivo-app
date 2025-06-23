package com.grupo12.clubdeportivoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PagoAdapter(private val pagos: List<HistorialPago>) :
    RecyclerView.Adapter<PagoAdapter.PagoViewHolder>() {

    class PagoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Asegúrate que estos IDs coincidan con tu layout item_pago.xml
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvMonto: TextView = itemView.findViewById(R.id.tvMonto)
        val tvMetodo: TextView = itemView.findViewById(R.id.tvMetodo)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvActividad: TextView = itemView.findViewById(R.id.tvActividad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pago, parent, false)
        return PagoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagoViewHolder, position: Int) {
        val pago = pagos[position]
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val fecha = try {
            LocalDate.parse(pago.fecha).format(dateFormatter)
        } catch (e: Exception) {
            pago.fecha // Si hay error al parsear, mostrar la fecha original
        }

        with(holder) {
            tvNombre.text = pago.nombre
            tvMonto.text = "$${"%.2f".format(pago.monto)}"
            tvMetodo.text = pago.metodoPago
            tvFecha.text = fecha
            tvActividad.text = "${pago.actividad} (${pago.frecuencia})"
        }
    }

    override fun getItemCount(): Int = pagos.size
}