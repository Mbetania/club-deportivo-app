package com.grupo12.clubdeportivoapp

import java.time.LocalDate

data class HistorialPago(
    val nombre: String,
    val monto: Double,
    val metodoPago: String,
    val fecha: String,  // Formato: yyyy-MM-dd
    val actividad: String,
    val frecuencia: String
)

data class SocioVencimiento(
    val nombre: String,
    val dni: String,
    val vencimiento: LocalDate
)

data class SocioCompleto(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val alias: String,
    val vencimiento: String,
    val actividad: String,
    val frecuencia: String,
    val mesesPagados: Int
)