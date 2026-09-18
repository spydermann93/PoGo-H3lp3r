package com.pogotcghelper.app.data.repository

import com.pogotcghelper.app.data.network.dto.CardBriefDto
import com.pogotcghelper.app.data.network.dto.CardDto
import com.pogotcghelper.app.domain.model.Attack
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.domain.model.TypeValue
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/** Preferred order of TCGplayer variants when a card is available in more than one finish. */
private val TCGPLAYER_VARIANT_PRIORITY = listOf(
    "holofoil", "reverseHolofoil", "normal", "1stEditionHolofoil", "unlimitedHolofoil",
)

/** TCGdex gives a bare image URL; a quality + format suffix must be appended to load it. */
private fun imageUrl(base: String?, quality: String): String =
    base?.let { "$it/$quality.webp" }.orEmpty()

/** Search results only carry id/name/image; open the card to load everything else. */
fun CardBriefDto.toDomain(): Card = Card(
    id = id,
    name = name,
    supertype = null,
    subtypes = emptyList(),
    hp = null,
    types = emptyList(),
    attacks = emptyList(),
    weaknesses = emptyList(),
    resistances = emptyList(),
    retreatCost = emptyList(),
    number = localId,
    artist = null,
    rarity = null,
    flavorText = null,
    setName = null,
    setSeries = null,
    smallImageUrl = imageUrl(image, "low"),
    largeImageUrl = imageUrl(image, "high"),
    tcgplayerMarketPriceUsd = null,
    tcgplayerUrl = null,
    cardmarketPriceEur = null,
    cardmarketUrl = null,
)

fun CardDto.toDomain(): Card {
    val tcgplayerPrice = pricing?.tcgplayer
        ?.let { prices ->
            TCGPLAYER_VARIANT_PRIORITY.firstNotNullOfOrNull { variant -> prices[variant]?.marketPrice }
                ?: prices.values.firstNotNullOfOrNull { it.marketPrice }
        }
    val cardmarketPrice = pricing?.cardmarket?.trend ?: pricing?.cardmarket?.avg

    return Card(
        id = id,
        name = name,
        supertype = category,
        subtypes = emptyList(),
        hp = hp?.toString(),
        types = types,
        attacks = attacks.map {
            Attack(
                name = it.name,
                cost = it.cost,
                damage = it.damage?.jsonPrimitive?.contentOrNull,
                text = it.effect,
            )
        },
        weaknesses = weaknesses.map { TypeValue(it.type, it.value.orEmpty()) },
        resistances = resistances.map { TypeValue(it.type, it.value.orEmpty()) },
        // TCGdex gives a numeric retreat cost rather than a list of energy types.
        retreatCost = List(retreat ?: 0) { "Colorless" },
        number = localId,
        artist = illustrator,
        rarity = rarity,
        flavorText = description,
        setName = set?.name,
        setSeries = null,
        smallImageUrl = imageUrl(image, "low"),
        largeImageUrl = imageUrl(image, "high"),
        tcgplayerMarketPriceUsd = tcgplayerPrice,
        tcgplayerUrl = null,
        cardmarketPriceEur = cardmarketPrice,
        cardmarketUrl = null,
    )
}
