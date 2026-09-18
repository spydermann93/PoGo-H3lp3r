package com.pogotcghelper.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class CollectionSummaryRow(
    val id: Long,
    val name: String,
    val cardCount: Int,
    val totalValue: Double,
)

@Dao
interface CollectionDao {

    @Query(
        """
        SELECT c.id AS id, c.name AS name,
               COALESCE(SUM(o.quantity), 0) AS cardCount,
               COALESCE(SUM(o.quantity * o.marketPrice), 0.0) AS totalValue
        FROM collections c
        LEFT JOIN owned_cards o ON o.collectionId = c.id
        GROUP BY c.id
        ORDER BY c.createdAtEpochMillis ASC
        """
    )
    fun observeSummaries(): Flow<List<CollectionSummaryRow>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun findById(id: Long): CollectionEntity?

    @Insert
    suspend fun insert(collection: CollectionEntity): Long

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteById(id: Long)
}
