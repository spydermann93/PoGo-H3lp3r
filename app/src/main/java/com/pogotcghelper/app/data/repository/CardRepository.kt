package com.pogotcghelper.app.data.repository

import com.pogotcghelper.app.data.network.TcgdexApi
import com.pogotcghelper.app.domain.model.Card

class CardRepository(private val api: TcgdexApi) {

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
        return response.map { it.toDomain() }
    }

    suspend fun getCard(id: String): Card =
        api.getCard(id).toDomain()

    /** Empty list on failure rather than propagating -- the rarity filter row just won't show. */
    suspend fun getRarities(): List<String> =
        runCatching { api.getRarities() }.getOrDefault(emptyList())
}
