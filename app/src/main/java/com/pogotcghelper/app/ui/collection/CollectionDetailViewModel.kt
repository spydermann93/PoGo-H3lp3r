package com.pogotcghelper.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CollectionMeta
import com.pogotcghelper.app.data.repository.CollectionRepository
import com.pogotcghelper.app.domain.model.Collection
import com.pogotcghelper.app.domain.model.OwnedCard
import com.pogotcghelper.app.domain.model.totalValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collectionName: String = "",
    val isTracker: Boolean = false,
    val cards: List<OwnedCard> = emptyList(),
    val totalValueUsd: Double = 0.0,
    /** Real (non-tracker) collections a tracker's card can be promoted into. */
    val realCollections: List<Collection> = emptyList(),
)

class CollectionDetailViewModel(
    private val collectionId: Long,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val meta = MutableStateFlow(CollectionMeta(name = "", isTracker = false))

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        meta,
        collectionRepository.observeCards(collectionId),
        collectionRepository.observeCollections(),
    ) { meta, cards, allCollections ->
        CollectionDetailUiState(
            collectionName = meta.name,
            isTracker = meta.isTracker,
            cards = cards,
            totalValueUsd = cards.totalValue,
            realCollections = allCollections.filter { !it.isTracker },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionDetailUiState())

    init {
        viewModelScope.launch {
            collectionRepository.getCollectionMeta(collectionId)?.let { meta.value = it }
        }
    }

    fun removeOne(cardId: String) {
        viewModelScope.launch { collectionRepository.removeOne(collectionId, cardId) }
    }

    /**
     * Ticks a card on/off. In a tracker this is purely a checklist mark, not an ownership
     * change. In a real collection it only ever fires from a quantity-0 row (a leftover
     * from before trackers existed, see MIGRATION_2_3) to bump it to owned; the screen
     * never wires this to an already-owned row there, so it can't zero one out by accident.
     */
    fun toggleTracked(cardId: String) {
        viewModelScope.launch { collectionRepository.toggleTracked(collectionId, cardId) }
    }

    fun removeAll(cardId: String) {
        viewModelScope.launch { collectionRepository.remove(collectionId, cardId) }
    }

    /** Records a tracker card as actually owned, in one of the user's real collections. */
    fun addToRealCollection(targetCollectionId: Long, card: OwnedCard) {
        viewModelScope.launch {
            collectionRepository.addOne(
                collectionId = targetCollectionId,
                cardId = card.cardId,
                name = card.name,
                setName = card.setName,
                imageUrl = card.imageUrl,
                marketPrice = card.marketPrice,
            )
        }
    }

    fun createRealCollectionAndAdd(name: String, card: OwnedCard) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val id = collectionRepository.createCollection(trimmed)
            collectionRepository.addOne(id, card.cardId, card.name, card.setName, card.imageUrl, card.marketPrice)
        }
    }
}
