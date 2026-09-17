package com.pogotcghelper.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnedCardDao {

    @Query("SELECT * FROM owned_cards ORDER BY addedAtEpochMillis DESC")
    fun observeAll(): Flow<List<OwnedCardEntity>>

    @Query("SELECT * FROM owned_cards WHERE cardId = :cardId")
    suspend fun findById(cardId: String): OwnedCardEntity?

    @Upsert
    suspend fun upsert(card: OwnedCardEntity)

    @Delete
    suspend fun delete(card: OwnedCardEntity)

    @Query("DELETE FROM owned_cards WHERE cardId = :cardId")
    suspend fun deleteById(cardId: String)
}
