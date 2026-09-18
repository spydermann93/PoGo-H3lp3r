package com.pogotcghelper.app.data.network.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** Minimal shape returned by the /cards search endpoint (TCGdex keeps list payloads small). */
@Serializable
data class CardBriefDto(
    val id: String,
    val localId: String? = null,
    val name: String,
    val image: String? = null,
)

/** Full shape returned by /cards/{id}. */
@Serializable
data class CardDto(
    val id: String,
    val localId: String? = null,
    val name: String,
    val image: String? = null,
    val category: String? = null,
    val illustrator: String? = null,
    val rarity: String? = null,
    val hp: Int? = null,
    val types: List<String> = emptyList(),
    val evolveFrom: String? = null,
    val description: String? = null,
    val attacks: List<AttackDto> = emptyList(),
    val weaknesses: List<TypeValueDto> = emptyList(),
    val resistances: List<TypeValueDto> = emptyList(),
    val retreat: Int? = null,
    val set: CardSetDto? = null,
    val pricing: PricingDto? = null,
)

@Serializable
data class AttackDto(
    val cost: List<String> = emptyList(),
    val name: String,
    val effect: String? = null,
    // TCGdex documents this as string | number depending on the card; decode loosely
    // and read the raw text back out in the mapper rather than risk a decode failure.
    val damage: JsonElement? = null,
)

@Serializable
data class TypeValueDto(
    val type: String,
    val value: String? = null,
)

@Serializable
data class CardSetDto(
    val id: String,
    val name: String,
    val logo: String? = null,
    val symbol: String? = null,
)

/** Shape returned by the /sets list endpoint -- just enough to name-lookup a set by id. */
@Serializable
data class SetBriefDto(
    val id: String,
    val name: String,
)

/** Full shape returned by /sets/{id}, including every card in the set (brief shape). */
@Serializable
data class SetDto(
    val id: String,
    val name: String,
    val cards: List<CardBriefDto> = emptyList(),
    val cardCount: SetCardCountDto? = null,
)

/**
 * "official" is the set's printed/base card count (what's on the card, e.g. "4/102");
 * "total" includes secret rares numbered past that. The gap between them is what
 * distinguishes a base-set import from a master-set one.
 */
@Serializable
data class SetCardCountDto(
    val official: Int? = null,
    val total: Int? = null,
)

@Serializable
data class PricingDto(
    // Not a plain variant->price map: TCGdex mixes "unit"/"updated" strings in as
    // siblings of the per-variant ("normal", "holofoil", ...) price objects, so a
    // typed Map<String, Dto> fails to decode. Read it as a raw object and pull
    // individual variants out by key in the mapper instead.
    val tcgplayer: JsonObject? = null,
    val cardmarket: CardmarketPricingDto? = null,
)

@Serializable
data class CardmarketPricingDto(
    val avg: Double? = null,
    val low: Double? = null,
    val trend: Double? = null,
    val avg1: Double? = null,
    val avg7: Double? = null,
    val avg30: Double? = null,
    val url: String? = null,
)
