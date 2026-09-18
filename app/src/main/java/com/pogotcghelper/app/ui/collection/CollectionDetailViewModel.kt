package com.pogotcghelper.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CollectionRepository
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
    val cards: List<OwnedCard> = emptyList(),
    val totalValueUsd: Double = 0.0,
)

class CollectionDetailViewModel(
    private val collectionId: Long,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val collectionName = MutableStateFlow("")

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        collectionName,
        collectionRepository.observeCards(collectionId),
    ) { name, cards ->
        CollectionDetailUiState(collectionName = name, cards = cards, totalValueUsd = cards.totalValue)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionDetailUiState())

    init {
        viewModelScope.launch {
            collectionName.value = collectionRepository.getCollectionName(collectionId).orEmpty()
        }
    }

    fun removeOne(cardId: String) {
        viewModelScope.launch { collectionRepository.removeOne(collectionId, cardId) }
    }

    fun markOwned(cardId: String) {
        viewModelScope.launch { collectionRepository.markOwned(collectionId, cardId) }
    }

    fun removeAll(cardId: String) {
        viewModelScope.launch { collectionRepository.remove(collectionId, cardId) }
    }
}
