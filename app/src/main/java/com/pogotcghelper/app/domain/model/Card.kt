package com.pogotcghelper.app.domain.model

data class Card(
    val id: String,
    val name: String,
    val supertype: String?,
    val subtypes: List<String>,
    val hp: String?,
    val types: List<String>,
    val attacks: List<Attack>,
    val weaknesses: List<TypeValue>,
    val resistances: List<TypeValue>,
    val retreatCost: List<String>,
    val number: String?,
    val artist: String?,
    val rarity: String?,
    val flavorText: String?,
    val setName: String?,
    val setSeries: String?,
    val smallImageUrl: String,
    val largeImageUrl: String,
    val tcgplayerMarketPriceUsd: Double?,
    val tcgplayerUrl: String?,
    val cardmarketPriceEur: Double?,
    val cardmarketUrl: String?,
)

data class Attack(
    val name: String,
    val cost: List<String>,
    val damage: String?,
    val text: String?,
)

data class TypeValue(
    val type: String,
    val value: String,
)

/** Best-effort market value for a card, preferring the USD TCGplayer price. */
fun Card.bestMarketPriceUsd(eurToUsdRate: Double = 1.08): Double? =
    tcgplayerMarketPriceUsd ?: cardmarketPriceEur?.let { it * eurToUsdRate }
