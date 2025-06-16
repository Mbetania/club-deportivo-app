package com.grupo12.clubdeportivoapp

import java.io.Serializable

data class Socio(
    val nombre: String,
    val apellido: String,
    val dni: String,
    val telefono: String,
    val email: String,
    val fechaNacimiento: String,
    val asociado: Boolean,
    val vencimiento: String
) : Serializable