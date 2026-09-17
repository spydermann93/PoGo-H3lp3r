package com.pogotcghelper.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_cards")
data class OwnedCardEntity(
    @PrimaryKey val cardId: String,
    val name: String,
    val setName: String?,
    val imageUrl: String,
    val quantity: Int,
    val marketPrice: Double?,
    val addedAtEpochMillis: Long,
)
