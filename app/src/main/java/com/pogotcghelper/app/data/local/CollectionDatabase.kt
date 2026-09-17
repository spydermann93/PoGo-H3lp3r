package com.pogotcghelper.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [OwnedCardEntity::class], version = 1, exportSchema = false)
abstract class CollectionDatabase : RoomDatabase() {
    abstract fun ownedCardDao(): OwnedCardDao

    companion object {
        const val DATABASE_NAME = "collection.db"
    }
}
