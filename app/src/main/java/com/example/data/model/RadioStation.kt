package com.example.data.model

import androidx.annotation.Keep

@Keep
data class RadioStation(
    val id: String,
    val name: String,
    val streamUrl: String,
    val imageUrl: String,
    val category: String,
    val country: String,
    val isFavorite: Boolean = false
)
