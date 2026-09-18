package com.pogotcghelper.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class CollectionSummaryRow(
    val id: Long,
    val name: String,
    val isTracker: Boolean,
    /** Total owned quantity for a regular collection; checked-off count for a tracker. */
    val cardCount: Int,
    val totalValue: Double,
    /** Row count for the collection -- the denominator ("out of N") for a tracker's progress. */
    val totalTracked: Int,
)

@Dao
interface CollectionDao {

    @Query(
        """
        SELECT c.id AS id, c.name AS name, c.isTracker AS isTracker,
               COALESCE(SUM(o.quantity), 0) AS cardCount,
               COALESCE(SUM(o.quantity * o.marketPrice), 0.0) AS totalValue,
               COUNT(o.cardId) AS totalTracked
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
