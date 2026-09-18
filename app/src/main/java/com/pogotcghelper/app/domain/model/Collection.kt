package com.pogotcghelper.app.domain.model

data class Collection(
    val id: Long,
    val name: String,
    val cardCount: Int,
    val totalValueUsd: Double,
)
