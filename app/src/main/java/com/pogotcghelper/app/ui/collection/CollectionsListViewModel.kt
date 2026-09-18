package com.pogotcghelper.app.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CollectionRepository
import com.pogotcghelper.app.domain.model.Collection
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionsListUiState(
    val collections: List<Collection> = emptyList(),
)

class CollectionsListViewModel(private val collectionRepository: CollectionRepository) : ViewModel() {

    val uiState: StateFlow<CollectionsListUiState> = collectionRepository.observeCollections()
        .map { CollectionsListUiState(collections = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionsListUiState())

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
}
