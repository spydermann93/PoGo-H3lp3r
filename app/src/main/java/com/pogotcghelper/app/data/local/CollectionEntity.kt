package com.pogotcghelper.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAtEpochMillis: Long,
    /**
     * A tracker is a set-completion checklist (from "Import a full set"): its cards are
     * never counted as owned anywhere else in the app. A regular (non-tracker) collection
     * is the user's actual inventory.
     */
    val isTracker: Boolean = false,
)
