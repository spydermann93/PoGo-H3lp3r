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

    /**
     * Every cardId actually owned (quantity > 0) in at least one real (non-tracker)
     * collection. Excludes quantity-0 rows (an unchecked checklist item) and, just as
     * importantly, excludes tracker collections entirely -- ticking a card off a set
     * checklist tracks progress, it isn't the same as owning the card.
     */
    @Query(
        """
        SELECT DISTINCT o.cardId FROM owned_cards o
        JOIN collections c ON c.id = o.collectionId
        WHERE o.quantity > 0 AND c.isTracker = 0
        """
    )
    fun observeAllOwnedCardIds(): Flow<List<String>>

    @Query("SELECT * FROM owned_cards WHERE collectionId = :collectionId AND cardId = :cardId")
    suspend fun find(collectionId: Long, cardId: String): OwnedCardEntity?

    @Query("SELECT cardId FROM owned_cards WHERE collectionId = :collectionId")
    suspend fun findCardIdsIn(collectionId: Long): List<String>

    @Upsert
    suspend fun upsert(card: OwnedCardEntity)

    @Upsert
    suspend fun upsertAll(cards: List<OwnedCardEntity>)

    @Query("DELETE FROM owned_cards WHERE collectionId = :collectionId AND cardId = :cardId")
    suspend fun delete(collectionId: Long, cardId: String)
}
