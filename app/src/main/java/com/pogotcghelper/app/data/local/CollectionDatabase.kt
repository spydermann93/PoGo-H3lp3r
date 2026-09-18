package com.pogotcghelper.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Database(
    entities = [CollectionEntity::class, OwnedCardEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class CollectionDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun ownedCardDao(): OwnedCardDao

    companion object {
        const val DATABASE_NAME = "collection.db"
        const val DEFAULT_COLLECTION_ID = 1L
        private const val DEFAULT_COLLECTION_NAME = "My Collection"

        /**
         * v1 had a single implicit collection (owned_cards keyed only by cardId).
         * v2 introduces named collections; existing rows are folded into one
         * default collection rather than wiping the user's data.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `collections` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAtEpochMillis` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "INSERT INTO `collections` (`id`, `name`, `createdAtEpochMillis`) VALUES ($DEFAULT_COLLECTION_ID, '$DEFAULT_COLLECTION_NAME', ${System.currentTimeMillis()})"
                )
                db.execSQL(
                    """
                    CREATE TABLE `owned_cards_new` (
                        `collectionId` INTEGER NOT NULL,
                        `cardId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `setName` TEXT,
                        `imageUrl` TEXT NOT NULL,
                        `quantity` INTEGER NOT NULL,
                        `marketPrice` REAL,
                        `addedAtEpochMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`collectionId`, `cardId`),
                        FOREIGN KEY(`collectionId`) REFERENCES `collections`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_owned_cards_collectionId` ON `owned_cards_new` (`collectionId`)")
                db.execSQL(
                    """
                    INSERT INTO `owned_cards_new`
                        (`collectionId`, `cardId`, `name`, `setName`, `imageUrl`, `quantity`, `marketPrice`, `addedAtEpochMillis`)
                    SELECT $DEFAULT_COLLECTION_ID, `cardId`, `name`, `setName`, `imageUrl`, `quantity`, `marketPrice`, `addedAtEpochMillis`
                    FROM `owned_cards`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `owned_cards`")
                db.execSQL("ALTER TABLE `owned_cards_new` RENAME TO `owned_cards`")
            }
        }
    }
}
