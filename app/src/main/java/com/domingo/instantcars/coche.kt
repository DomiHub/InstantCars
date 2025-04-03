package com.domingo.instantcars

data class Coche(
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val precio: String = "",
    val km: String = "",
    val imagen: String = "",
    val subidoPor: String = "", // UID del usuario que lo ha subido
    val descripcion: String = "",
    val ubicacion: String = ""
)
