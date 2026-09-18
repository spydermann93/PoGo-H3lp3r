package com.pogotcghelper.app.data.repository

import com.pogotcghelper.app.data.local.CollectionDao
import com.pogotcghelper.app.data.local.CollectionEntity
import com.pogotcghelper.app.data.local.CollectionSummaryRow
import com.pogotcghelper.app.data.local.OwnedCardDao
import com.pogotcghelper.app.data.local.OwnedCardEntity
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.domain.model.Collection
import com.pogotcghelper.app.domain.model.OwnedCard
import com.pogotcghelper.app.domain.model.bestMarketPriceUsd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val ownedCardDao: OwnedCardDao,
) {

    fun observeCollections(): Flow<List<Collection>> =
        collectionDao.observeSummaries().map { rows -> rows.map { it.toDomain() } }

    suspend fun createCollection(name: String): Long =
        collectionDao.insert(CollectionEntity(name = name, createdAtEpochMillis = System.currentTimeMillis()))

    suspend fun deleteCollection(collectionId: Long) = collectionDao.deleteById(collectionId)

    suspend fun getCollectionName(collectionId: Long): String? =
        collectionDao.findById(collectionId)?.name

    fun observeCards(collectionId: Long): Flow<List<OwnedCard>> =
        ownedCardDao.observeByCollection(collectionId).map { entities -> entities.map { it.toDomain() } }

    /** collectionId -> quantity owned of this card, for collections that have at least one. */
    fun observeQuantitiesForCard(cardId: String): Flow<Map<Long, Int>> =
        ownedCardDao.observeQuantitiesForCard(cardId)
            .map { rows -> rows.associate { it.collectionId to it.quantity } }

    suspend fun addOne(collectionId: Long, card: Card) {
        val existing = ownedCardDao.find(collectionId, card.id)
        val newQuantity = (existing?.quantity ?: 0) + 1
        ownedCardDao.upsert(
            OwnedCardEntity(
                collectionId = collectionId,
                cardId = card.id,
                name = card.name,
                setName = card.setName,
                imageUrl = card.smallImageUrl,
                quantity = newQuantity,
                marketPrice = card.bestMarketPriceUsd(),
                addedAtEpochMillis = existing?.addedAtEpochMillis ?: System.currentTimeMillis(),
            )
        )
    }

    suspend fun removeOne(collectionId: Long, cardId: String) {
        val existing = ownedCardDao.find(collectionId, cardId) ?: return
        if (existing.quantity <= 1) {
            ownedCardDao.delete(collectionId, cardId)
        } else {
            ownedCardDao.upsert(existing.copy(quantity = existing.quantity - 1))
        }
    }

    suspend fun remove(collectionId: Long, cardId: String) = ownedCardDao.delete(collectionId, cardId)
}

private fun CollectionSummaryRow.toDomain() = Collection(
    id = id,
    name = name,
    cardCount = cardCount,
    totalValueUsd = totalValue,
)

private fun OwnedCardEntity.toDomain() = OwnedCard(
    cardId = cardId,
    name = name,
    setName = setName,
    imageUrl = imageUrl,
    quantity = quantity,
    marketPrice = marketPrice,
)
