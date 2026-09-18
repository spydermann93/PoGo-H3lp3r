package com.pogotcghelper.app.data.network

import com.pogotcghelper.app.data.network.dto.CardBriefDto
import com.pogotcghelper.app.data.network.dto.CardDto
import com.pogotcghelper.app.data.network.dto.SetBriefDto
import com.pogotcghelper.app.data.network.dto.SetDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Client for the free, open-source TCGdex API (https://tcgdex.dev). No API key or
 * account required.
 */
interface TcgdexApi {

    @GET("cards")
    suspend fun searchCards(
        @Query("name") name: String,
        @Query("rarity") rarity: String? = null,
        @Query("sort:field") sortField: String = "name",
        @Query("sort:order") sortOrder: String = "ASC",
        @Query("pagination:page") page: Int = 1,
        @Query("pagination:itemsPerPage") itemsPerPage: Int = 30,
    ): List<CardBriefDto>

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") id: String): CardDto

    /** Canonical list of rarity strings this API recognizes, for building a filter UI. */
    @GET("rarities")
    suspend fun getRarities(): List<String>

    /** Every set, id + name only -- used to label search results, which omit set info. */
    @GET("sets")
    suspend fun getSets(): List<SetBriefDto>

    /** A single set with every card in it, for bulk-importing a full set into a collection. */
    @GET("sets/{id}")
    suspend fun getSet(@Path("id") id: String): SetDto

    companion object {
        const val BASE_URL = "https://api.tcgdex.net/v2/en/"
    }
}
