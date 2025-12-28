package com.example.weather

data class City(
    val name: String,
    val id: Int,
    var temperature: Double? = null
)
