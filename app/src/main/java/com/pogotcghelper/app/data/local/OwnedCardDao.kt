package com.pogotcghelper.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

data class CardQuantityRow(val collectionId: Long, val quantity: Int)

@Dao
interface OwnedCardDao {

    @Query("SELECT * FROM owned_cards WHERE collectionId = :collectionId ORDER BY addedAtEpochMillis DESC")
    fun observeByCollection(collectionId: Long): Flow<List<OwnedCardEntity>>

    @Query("SELECT collectionId, quantity FROM owned_cards WHERE cardId = :cardId")
    fun observeQuantitiesForCard(cardId: String): Flow<List<CardQuantityRow>>

    /** Every cardId owned in at least one collection, regardless of which. */
    @Query("SELECT DISTINCT cardId FROM owned_cards")
    fun observeAllOwnedCardIds(): Flow<List<String>>

    @Query("SELECT * FROM owned_cards WHERE collectionId = :collectionId AND cardId = :cardId")
    suspend fun find(collectionId: Long, cardId: String): OwnedCardEntity?

    @Upsert
    suspend fun upsert(card: OwnedCardEntity)

    @Query("DELETE FROM owned_cards WHERE collectionId = :collectionId AND cardId = :cardId")
    suspend fun delete(collectionId: Long, cardId: String)
}
