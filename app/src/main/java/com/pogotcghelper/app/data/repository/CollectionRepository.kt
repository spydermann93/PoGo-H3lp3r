package com.pogotcghelper.app.data.repository

import com.pogotcghelper.app.data.local.OwnedCardDao
import com.pogotcghelper.app.data.local.OwnedCardEntity
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.domain.model.OwnedCard
import com.pogotcghelper.app.domain.model.bestMarketPriceUsd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CollectionRepository(private val dao: OwnedCardDao) {

    fun observeCollection(): Flow<List<OwnedCard>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun quantityFor(cardId: String): Int =
        dao.findById(cardId)?.quantity ?: 0

    suspend fun addOne(card: Card) {
        val existing = dao.findById(card.id)
        val newQuantity = (existing?.quantity ?: 0) + 1
        dao.upsert(
            OwnedCardEntity(
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

    suspend fun removeOne(cardId: String) {
        val existing = dao.findById(cardId) ?: return
        if (existing.quantity <= 1) {
            dao.deleteById(cardId)
        } else {
            dao.upsert(existing.copy(quantity = existing.quantity - 1))
        }
    }

    suspend fun remove(cardId: String) = dao.deleteById(cardId)
}

private fun OwnedCardEntity.toDomain() = OwnedCard(
    cardId = cardId,
    name = name,
    setName = setName,
    imageUrl = imageUrl,
    quantity = quantity,
    marketPrice = marketPrice,
)
