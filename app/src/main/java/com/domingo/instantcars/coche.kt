package com.domingo.instantcars

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
    @Transient val id: String? = null  // El id del documento en Firestore (no se sube a Firestore)
)
