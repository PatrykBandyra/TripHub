package com.example.triphub.models.weather

import java.io.Serializable

data class Minutely(
    val dt: Long,
    val precipitation: Double
) : Serializable
