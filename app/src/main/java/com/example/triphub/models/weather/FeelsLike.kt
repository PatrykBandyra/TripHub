package com.example.triphub.models.weather

import java.io.Serializable

data class FeelsLike(
    val day: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
) : Serializable
