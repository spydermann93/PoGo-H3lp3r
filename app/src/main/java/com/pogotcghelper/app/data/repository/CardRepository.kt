package com.pogotcghelper.app.data.repository

import com.pogotcghelper.app.data.network.TcgdexApi
import com.pogotcghelper.app.data.network.dto.CardBriefDto
import com.pogotcghelper.app.domain.model.Card

class CardRepository(private val api: TcgdexApi) {

    /** Loaded once and cached -- search results carry only an id, not a set name. */
    private var cachedSetNames: Map<String, String>? = null

    suspend fun searchByName(
        name: String,
        rarity: String? = null,
        sortDescending: Boolean = false,
        page: Int = 1,
    ): List<Card> {
        if (name.isBlank()) return emptyList()
        val escaped = name.trim().replace("*", "")
        val response = api.searchCards(
            name = "*$escaped*",
            rarity = rarity,
            sortOrder = if (sortDescending) "DESC" else "ASC",
            page = page,
        )
        val setNames = loadSetNames()
        return response.map { brief -> brief.toDomain(setNameFor(brief, setNames)) }
    }

    suspend fun getCard(id: String): Card =
        api.getCard(id).toDomain()

    /** Empty list on failure rather than propagating -- the rarity filter row just won't show. */
    suspend fun getRarities(): List<String> =
        runCatching { api.getRarities() }.getOrDefault(emptyList())

    private suspend fun loadSetNames(): Map<String, String> {
        cachedSetNames?.let { return it }
        val names = runCatching { api.getSets() }.getOrDefault(emptyList())
            .associate { it.id to it.name }
        cachedSetNames = names
        return names
    }

    /** A card's id is "<setId>-<localId>" (e.g. "base1-4"); strip the localId to get the set id. */
    private fun setNameFor(brief: CardBriefDto, setNames: Map<String, String>): String? {
        val localId = brief.localId ?: return null
        val setId = brief.id.removeSuffix("-$localId")
        return setNames[setId]
    }
}
