package com.pogotcghelper.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.pogotcghelper.app.data.local.CollectionDatabase
import com.pogotcghelper.app.data.network.RetryOnServerErrorInterceptor
import com.pogotcghelper.app.data.network.TcgdexApi
import com.pogotcghelper.app.data.repository.CardRepository
import com.pogotcghelper.app.data.repository.CollectionRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

/**
 * Minimal hand-rolled dependency container. Kept simple on purpose rather than
 * pulling in a DI framework for a small, single-module app.
 */
class AppContainer(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(RetryOnServerErrorInterceptor())
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        )
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(TcgdexApi.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val api: TcgdexApi = retrofit.create(TcgdexApi::class.java)

    private val database = Room.databaseBuilder(
        context.applicationContext,
        CollectionDatabase::class.java,
        CollectionDatabase.DATABASE_NAME,
    )
        .addMigrations(CollectionDatabase.MIGRATION_1_2)
        .addCallback(object : RoomDatabase.Callback() {
            // Fresh installs start directly at the latest schema, so MIGRATION_1_2
            // never runs for them; seed the same default collection here instead.
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO `collections` (`id`, `name`, `createdAtEpochMillis`) VALUES " +
                        "(${CollectionDatabase.DEFAULT_COLLECTION_ID}, 'My Collection', ${System.currentTimeMillis()})"
                )
            }
        })
        .build()

    val cardRepository = CardRepository(api)
    val collectionRepository = CollectionRepository(database.collectionDao(), database.ownedCardDao())
}
