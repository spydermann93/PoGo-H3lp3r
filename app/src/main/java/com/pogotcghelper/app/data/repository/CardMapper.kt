package com.pogotcghelper.app.data.repository

import com.pogotcghelper.app.data.network.dto.CardDto
import com.pogotcghelper.app.domain.model.Attack
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.domain.model.TypeValue

/** Preferred order of TCGplayer variants when a card is available in more than one finish. */
private val TCGPLAYER_VARIANT_PRIORITY = listOf(
    "holofoil", "reverseHolofoil", "normal", "1stEditionHolofoil", "unlimitedHolofoil",
)

fun CardDto.toDomain(): Card {
    val tcgplayerPrice = tcgplayer?.prices
        ?.let { prices ->
            TCGPLAYER_VARIANT_PRIORITY.firstNotNullOfOrNull { variant -> prices[variant]?.market }
                ?: prices.values.firstNotNullOfOrNull { it.market }
        }
    val cardmarketPrice = cardmarket?.prices?.trendPrice ?: cardmarket?.prices?.averageSellPrice

    return Card(
        id = id,
        name = name,
        supertype = supertype,
        subtypes = subtypes,
        hp = hp,
        types = types,
        attacks = attacks.map { Attack(name = it.name, cost = it.cost, damage = it.damage, text = it.text) },
        weaknesses = weaknesses.map { TypeValue(it.type, it.value) },
        resistances = resistances.map { TypeValue(it.type, it.value) },
        retreatCost = retreatCost,
        number = number,
        artist = artist,
        rarity = rarity,
        flavorText = flavorText,
        setName = set?.name,
        setSeries = set?.series,
        smallImageUrl = images.small,
        largeImageUrl = images.large,
        tcgplayerMarketPriceUsd = tcgplayerPrice,
        tcgplayerUrl = tcgplayer?.url,
        cardmarketPriceEur = cardmarketPrice,
        cardmarketUrl = cardmarket?.url,
    )
}
