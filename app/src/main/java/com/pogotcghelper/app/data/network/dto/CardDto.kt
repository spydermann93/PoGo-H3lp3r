package com.pogotcghelper.app.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CardListResponseDto(
    val data: List<CardDto> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 0,
    val count: Int = 0,
    val totalCount: Int = 0,
)

@Serializable
data class CardResponseDto(
    val data: CardDto,
)

@Serializable
data class CardDto(
    val id: String,
    val name: String,
    val supertype: String? = null,
    val subtypes: List<String> = emptyList(),
    val hp: String? = null,
    val types: List<String> = emptyList(),
    val evolvesFrom: String? = null,
    val attacks: List<AttackDto> = emptyList(),
    val weaknesses: List<TypeValueDto> = emptyList(),
    val resistances: List<TypeValueDto> = emptyList(),
    val retreatCost: List<String> = emptyList(),
    val number: String? = null,
    val artist: String? = null,
    val rarity: String? = null,
    val flavorText: String? = null,
    val set: CardSetDto? = null,
    val images: CardImagesDto,
    val tcgplayer: TcgplayerDto? = null,
    val cardmarket: CardmarketDto? = null,
)

@Serializable
data class AttackDto(
    val name: String,
    val cost: List<String> = emptyList(),
    val convertedEnergyCost: Int = 0,
    val damage: String? = null,
    val text: String? = null,
)

@Serializable
data class TypeValueDto(
    val type: String,
    val value: String,
)

@Serializable
data class CardSetDto(
    val id: String,
    val name: String,
    val series: String? = null,
    val releaseDate: String? = null,
    val images: CardSetImagesDto? = null,
)

@Serializable
data class CardSetImagesDto(
    val symbol: String? = null,
    val logo: String? = null,
)

@Serializable
data class CardImagesDto(
    val small: String,
    val large: String,
)

@Serializable
data class TcgplayerDto(
    val url: String? = null,
    val updatedAt: String? = null,
    val prices: Map<String, TcgplayerPriceDto> = emptyMap(),
)

@Serializable
data class TcgplayerPriceDto(
    val low: Double? = null,
    val mid: Double? = null,
    val high: Double? = null,
    val market: Double? = null,
    val directLow: Double? = null,
)

@Serializable
data class CardmarketDto(
    val url: String? = null,
    val updatedAt: String? = null,
    val prices: CardmarketPricesDto? = null,
)

@Serializable
data class CardmarketPricesDto(
    val averageSellPrice: Double? = null,
    val lowPrice: Double? = null,
    val trendPrice: Double? = null,
    val suggestedPrice: Double? = null,
    val avg1: Double? = null,
    val avg7: Double? = null,
    val avg30: Double? = null,
)
