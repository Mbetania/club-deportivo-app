package com.grupo12.clubdeportivoapp

import java.io.Serializable

data class Socio(
    val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val telefono: String = "",
    val email: String = "",
    val fechaNacimiento: String = "",
    val asociado: Boolean = true,
    val vencimiento: String? = null,
    val alias: String
) : Serializable {
    val nombreCompleto: String
        get() = "$nombre $apellido"
}
