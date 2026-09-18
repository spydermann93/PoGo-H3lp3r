package com.pogotcghelper.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "owned_cards",
    primaryKeys = ["collectionId", "cardId"],
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("collectionId")],
)
data class OwnedCardEntity(
    val collectionId: Long,
    val cardId: String,
    val name: String,
    val setName: String?,
    val imageUrl: String,
    val quantity: Int,
    val marketPrice: Double?,
    val addedAtEpochMillis: Long,
)
