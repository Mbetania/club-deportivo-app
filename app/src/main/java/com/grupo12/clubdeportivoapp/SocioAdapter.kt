package com.grupo12.clubdeportivoapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SocioAdapter(
    context: Context,
    private val socios: List<Socio>,
    private val onItemClick: (Socio) -> Unit
) : ArrayAdapter<Socio>(context, 0, socios) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val socio = getItem(position) ?: return view
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "${socio.nombre} ${socio.apellido} - DNI: ${socio.dni}"
        textView.setTextColor(context.getColor(R.color.primary_text))

        view.setOnClickListener { onItemClick(socio) }

        return view
    }
}