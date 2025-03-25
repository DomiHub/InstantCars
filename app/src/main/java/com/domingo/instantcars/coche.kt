package com.domingo.instantcars

data class Coche(
    val marca: String = "",
    val modelo: String = "",
    val anio: Int = 0,
    val precio: Double = 0.0,
    val imagen: String = "" // URL de la imagen en Firebase Storage
)
