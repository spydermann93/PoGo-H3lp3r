package com.pogotcghelper.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CardRepository
import com.pogotcghelper.app.data.repository.CollectionRepository
import com.pogotcghelper.app.domain.model.Collection
import com.pogotcghelper.app.domain.model.TcgSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionsListUiState(
    val collections: List<Collection> = emptyList(),
    val availableSets: List<TcgSet> = emptyList(),
)

class CollectionsListViewModel(
    private val collectionRepository: CollectionRepository,
    private val cardRepository: CardRepository,
) : ViewModel() {

    private val availableSets = MutableStateFlow<List<TcgSet>>(emptyList())

    val uiState: StateFlow<CollectionsListUiState> = combine(
        collectionRepository.observeCollections(),
        availableSets,
    ) { collections, sets ->
        CollectionsListUiState(collections = collections, availableSets = sets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsListUiState())

    init {
        viewModelScope.launch { availableSets.value = cardRepository.getSets() }
    }

    fun createCollection(name: String, onCreated: (Long) -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val id = collectionRepository.createCollection(trimmed)
            onCreated(id)
        }
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch { collectionRepository.deleteCollection(collectionId) }
    }

    /**
     * Imports a set as a completion-tracking checklist, never as owned cards. [targetCollectionId]
     * adds into an existing tracker, or [newCollectionName] creates a new one.
     */
    fun importSet(
        setId: String,
        baseSetOnly: Boolean,
        targetCollectionId: Long?,
        newCollectionName: String?,
        onDone: (Long) -> Unit,
    ) {
        viewModelScope.launch {
            val collectionId = targetCollectionId
                ?: collectionRepository.createCollection(newCollectionName.orEmpty().trim(), isTracker = true)
            val cards = cardRepository.getSetCards(setId, baseSetOnly)
            collectionRepository.addSet(collectionId, cards)
            onDone(collectionId)
        }
    }
}
