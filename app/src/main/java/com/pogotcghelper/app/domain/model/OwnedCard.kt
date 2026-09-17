package com.pogotcghelper.app.domain.model

data class OwnedCard(
    val cardId: String,
    val name: String,
    val setName: String?,
    val imageUrl: String,
    val quantity: Int,
    val marketPrice: Double?,
)

val List<OwnedCard>.totalValue: Double
    get() = sumOf { (it.marketPrice ?: 0.0) * it.quantity }
