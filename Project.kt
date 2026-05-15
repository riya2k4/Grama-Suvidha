package com.example.gramasuvidha.model

data class Project(
    val id: Int,
    val title: String,
    val titleKn: String,
    val description: String,
    val descriptionKn: String,
    val location: String,
    val locationKn: String,
    val budget: String,
    val status: String,
    val progress: Float,
    val completionDate: String,
    val imageUrl: String? = null,
    val beforeImageUrl: String? = null,
    val afterImageUrl: String? = null,
    val rating: Int = 0,
)
