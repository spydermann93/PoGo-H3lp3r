package com.pogotcghelper.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CardRepository
import com.pogotcghelper.app.data.repository.CollectionRepository
import com.pogotcghelper.app.domain.model.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CardDetailUiState(
    val isLoading: Boolean = true,
    val card: Card? = null,
    val ownedQuantity: Int = 0,
    val error: String? = null,
)

class CardDetailViewModel(
    private val cardId: String,
    private val cardRepository: CardRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { cardRepository.getCard(cardId) }
                .onSuccess { card ->
                    val owned = collectionRepository.quantityFor(cardId)
                    _uiState.update { it.copy(isLoading = false, card = card, ownedQuantity = owned) }
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load card") }
                }
        }
    }

    fun addToCollection() {
        val card = _uiState.value.card ?: return
        viewModelScope.launch {
            collectionRepository.addOne(card)
            _uiState.update { it.copy(ownedQuantity = it.ownedQuantity + 1) }
        }
    }

    fun removeFromCollection() {
        if (_uiState.value.ownedQuantity <= 0) return
        viewModelScope.launch {
            collectionRepository.removeOne(cardId)
            _uiState.update { it.copy(ownedQuantity = (it.ownedQuantity - 1).coerceAtLeast(0)) }
        }
    }
}
