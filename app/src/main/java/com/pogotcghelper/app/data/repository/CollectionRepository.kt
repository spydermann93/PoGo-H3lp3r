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

data class CollectionMeta(val name: String, val isTracker: Boolean)

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val ownedCardDao: OwnedCardDao,
) {

    fun observeCollections(): Flow<List<Collection>> =
        collectionDao.observeSummaries().map { rows -> rows.map { it.toDomain() } }

    suspend fun createCollection(name: String, isTracker: Boolean = false): Long =
        collectionDao.insert(
            CollectionEntity(name = name, createdAtEpochMillis = System.currentTimeMillis(), isTracker = isTracker)
        )

    suspend fun deleteCollection(collectionId: Long) = collectionDao.deleteById(collectionId)

    suspend fun getCollectionMeta(collectionId: Long): CollectionMeta? =
        collectionDao.findById(collectionId)?.let { CollectionMeta(it.name, it.isTracker) }

    fun observeCards(collectionId: Long): Flow<List<OwnedCard>> =
        ownedCardDao.observeByCollection(collectionId).map { entities -> entities.map { it.toDomain() } }

    /** collectionId -> quantity owned of this card, for collections that have at least one. */
    fun observeQuantitiesForCard(cardId: String): Flow<Map<Long, Int>> =
        ownedCardDao.observeQuantitiesForCard(cardId)
            .map { rows -> rows.associate { it.collectionId to it.quantity } }

    /** Every cardId owned in a real (non-tracker) collection, for the search ownership filter. */
    fun observeOwnedCardIds(): Flow<Set<String>> =
        ownedCardDao.observeAllOwnedCardIds().map { it.toSet() }

    suspend fun addOne(collectionId: Long, card: Card) {
        addOne(collectionId, card.id, card.name, card.setName, card.smallImageUrl, card.bestMarketPriceUsd())
    }

    /** Records a card as owned using already-known fields, e.g. promoting a tracker entry. */
    suspend fun addOne(
        collectionId: Long,
        cardId: String,
        name: String,
        setName: String?,
        imageUrl: String,
        marketPrice: Double?,
    ) {
        val existing = ownedCardDao.find(collectionId, cardId)
        val newQuantity = (existing?.quantity ?: 0) + 1
        ownedCardDao.upsert(
            OwnedCardEntity(
                collectionId = collectionId,
                cardId = cardId,
                name = name,
                setName = setName,
                imageUrl = imageUrl,
                quantity = newQuantity,
                marketPrice = marketPrice,
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

    /** Flips a tracker entry between unchecked (0) and checked (1) -- a checklist tick, not a real quantity. */
    suspend fun toggleTracked(collectionId: Long, cardId: String) {
        val existing = ownedCardDao.find(collectionId, cardId) ?: return
        ownedCardDao.upsert(existing.copy(quantity = if (existing.quantity > 0) 0 else 1))
    }

    /**
     * Bulk-adds every card of a set into a tracker collection as an unchecked (quantity 0)
     * checklist entry. This never touches a real collection or counts as owning the card --
     * it's purely a "how close am I to completing this set" progress tool. Cards already
     * tracked in this collection are left untouched rather than overwritten.
     */
    suspend fun addSet(collectionId: Long, cards: List<Card>) {
        val existingIds = ownedCardDao.findCardIdsIn(collectionId).toSet()
        val addedAt = System.currentTimeMillis()
        val newRows = cards.filter { it.id !in existingIds }.map { card ->
            OwnedCardEntity(
                collectionId = collectionId,
                cardId = card.id,
                name = card.name,
                setName = card.setName,
                imageUrl = card.smallImageUrl,
                quantity = 0,
                marketPrice = card.bestMarketPriceUsd(),
                addedAtEpochMillis = addedAt,
            )
        }
        if (newRows.isNotEmpty()) ownedCardDao.upsertAll(newRows)
    }
}

private fun CollectionSummaryRow.toDomain() = Collection(
    id = id,
    name = name,
    isTracker = isTracker,
    cardCount = cardCount,
    totalValueUsd = totalValue,
    totalTracked = totalTracked,
)

private fun OwnedCardEntity.toDomain() = OwnedCard(
    cardId = cardId,
    name = name,
    setName = setName,
    imageUrl = imageUrl,
    quantity = quantity,
    marketPrice = marketPrice,
)
