package com.pogotcghelper.app.data.network

import com.pogotcghelper.app.data.network.dto.CardBriefDto
import com.pogotcghelper.app.data.network.dto.CardDto
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
        @Query("sort:field") sortField: String = "name",
        @Query("sort:order") sortOrder: String = "ASC",
        @Query("pagination:page") page: Int = 1,
        @Query("pagination:itemsPerPage") itemsPerPage: Int = 30,
    ): List<CardBriefDto>

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") id: String): CardDto

    companion object {
        const val BASE_URL = "https://api.tcgdex.net/v2/en/"
    }
}
