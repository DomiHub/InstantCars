package com.domingo.instantcars.vehiculos

data class Coche(
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val precio: String = "",
    val km: String = "",
    val imagen: String = "",
    val subidoPor: String = "",
    val subidoPorNombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    @Transient val id: String? = null
)
