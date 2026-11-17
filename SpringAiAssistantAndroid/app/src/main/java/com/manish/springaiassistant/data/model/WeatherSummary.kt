package com.manish.springaiassistant.data.model

data class WeatherSummary(
    val city: String,
    val temperature: Double,
    val humidity: Int,
    val condition: String,
    val recommendation: String
)