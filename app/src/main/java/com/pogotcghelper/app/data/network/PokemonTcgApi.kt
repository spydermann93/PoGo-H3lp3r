package com.pogotcghelper.app.data.network

import com.pogotcghelper.app.data.network.dto.CardListResponseDto
import com.pogotcghelper.app.data.network.dto.CardResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Client for the public Pokémon TCG API (https://pokemontcg.io).
 * No API key is required for low-volume, non-commercial use.
 */
interface PokemonTcgApi {

    @GET("cards")
    suspend fun searchCards(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 30,
        @Query("orderBy") orderBy: String = "name",
    ): CardListResponseDto

    @GET("cards/{id}")
    suspend fun getCard(@Path("id") id: String): CardResponseDto

    companion object {
        const val BASE_URL = "https://api.pokemontcg.io/v2/"
    }
}
