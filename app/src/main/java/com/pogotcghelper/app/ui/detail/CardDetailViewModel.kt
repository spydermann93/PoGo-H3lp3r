package com.pogotcghelper.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CardRepository
import com.pogotcghelper.app.data.repository.CollectionRepository
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.domain.model.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CardDetailUiState(
    val isLoading: Boolean = true,
    val card: Card? = null,
    val collections: List<Collection> = emptyList(),
    val quantitiesByCollection: Map<Long, Int> = emptyMap(),
    val error: String? = null,
)

class CardDetailViewModel(
    private val cardId: String,
    private val cardRepository: CardRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private val cardState = MutableStateFlow<Card?>(null)
    private val loadingState = MutableStateFlow(true)
    private val errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CardDetailUiState> = combine(
        cardState,
        loadingState,
        errorState,
        collectionRepository.observeCollections(),
        collectionRepository.observeQuantitiesForCard(cardId),
    ) { card, isLoading, error, collections, quantities ->
        CardDetailUiState(
            isLoading = isLoading,
            card = card,
            // Trackers are a set-completion checklist, not somewhere a searched card gets
            // "added" in the usual sense -- only offer real collections here.
            collections = collections.filter { !it.isTracker },
            quantitiesByCollection = quantities,
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CardDetailUiState())

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            loadingState.value = true
            errorState.value = null
            runCatching { cardRepository.getCard(cardId) }
                .onSuccess {
                    cardState.value = it
                    loadingState.value = false
                }
                .onFailure {
                    errorState.value = it.message ?: "Failed to load card"
                    loadingState.value = false
                }
        }
    }

    fun addToCollection(collectionId: Long) {
        val card = cardState.value ?: return
        viewModelScope.launch { collectionRepository.addOne(collectionId, card) }
    }

    fun removeFromCollection(collectionId: Long) {
        viewModelScope.launch { collectionRepository.removeOne(collectionId, cardId) }
    }

    fun createCollectionAndAdd(name: String) {
        val card = cardState.value ?: return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val id = collectionRepository.createCollection(trimmed)
            collectionRepository.addOne(id, card)
        }
    }
}
