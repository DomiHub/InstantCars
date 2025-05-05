package com.domingo.instantcars

import java.util.Date

data class Mensaje(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Date? = null
)
