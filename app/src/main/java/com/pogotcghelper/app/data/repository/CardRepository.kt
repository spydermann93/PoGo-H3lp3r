package com.pogotcghelper.app.data.repository

import com.pogotcghelper.app.data.network.TcgdexApi
import com.pogotcghelper.app.data.network.dto.CardBriefDto
import com.pogotcghelper.app.data.network.dto.SetBriefDto
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.domain.model.TcgSet

class CardRepository(private val api: TcgdexApi) {

    /** Loaded once and cached -- search results carry only an id, not a set name. */
    private var cachedSets: List<SetBriefDto>? = null

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
        val setNames = loadSets().associate { it.id to it.name }
        return response.map { brief -> brief.toDomain(setNameFor(brief, setNames)) }
    }

    suspend fun getCard(id: String): Card =
        api.getCard(id).toDomain()

    /** Empty list on failure rather than propagating -- the rarity filter row just won't show. */
    suspend fun getRarities(): List<String> =
        runCatching { api.getRarities() }.getOrDefault(emptyList())

    /** Every real TCG set, for a "pick a set to import" picker. */
    suspend fun getSets(): List<TcgSet> =
        loadSets().map { TcgSet(it.id, it.name) }

    /**
     * Every card in a set, for bulk-importing a full set into a collection. These come from
     * the set's own brief card list, so -- same as search results -- they carry no pricing;
     * fetching prices for a whole set (up to ~250 cards) one at a time isn't worth the requests.
     */
    suspend fun getSetCards(setId: String): List<Card> {
        val set = api.getSet(setId)
        return set.cards.map { it.toDomain(set.name) }
    }

    private suspend fun loadSets(): List<SetBriefDto> {
        cachedSets?.let { return it }
        val sets = runCatching { api.getSets() }.getOrDefault(emptyList())
        cachedSets = sets
        return sets
    }

    /** A card's id is "<setId>-<localId>" (e.g. "base1-4"); strip the localId to get the set id. */
    private fun setNameFor(brief: CardBriefDto, setNames: Map<String, String>): String? {
        val localId = brief.localId ?: return null
        val setId = brief.id.removeSuffix("-$localId")
        return setNames[setId]
    }
}
