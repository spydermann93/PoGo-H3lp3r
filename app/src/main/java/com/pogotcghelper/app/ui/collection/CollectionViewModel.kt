package com.pogotcghelper.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CollectionRepository
import com.pogotcghelper.app.domain.model.OwnedCard
import com.pogotcghelper.app.domain.model.totalValue
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionUiState(
    val cards: List<OwnedCard> = emptyList(),
    val totalValueUsd: Double = 0.0,
)

class CollectionViewModel(private val collectionRepository: CollectionRepository) : ViewModel() {

    val uiState: StateFlow<CollectionUiState> = collectionRepository.observeCollection()
        .map { cards -> CollectionUiState(cards = cards, totalValueUsd = cards.totalValue) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionUiState())

    fun removeOne(cardId: String) {
        viewModelScope.launch { collectionRepository.removeOne(cardId) }
    }

    fun removeAll(cardId: String) {
        viewModelScope.launch { collectionRepository.remove(cardId) }
    }
}
