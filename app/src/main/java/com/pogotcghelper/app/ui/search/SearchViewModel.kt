package com.pogotcghelper.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CardRepository
import com.pogotcghelper.app.data.repository.CollectionRepository
import com.pogotcghelper.app.domain.model.Card
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OwnershipFilter { ALL, OWNED, NOT_OWNED }

data class SearchUiState(
    val query: String = "",
    val ownershipFilter: OwnershipFilter = OwnershipFilter.ALL,
    val sortDescending: Boolean = false,
    val isLoading: Boolean = false,
    val results: List<Card> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false,
)

private data class InternalState(
    val query: String = "",
    val ownershipFilter: OwnershipFilter = OwnershipFilter.ALL,
    val sortDescending: Boolean = false,
    val isLoading: Boolean = false,
    val rawResults: List<Card> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false,
)

private const val DEBOUNCE_MILLIS = 400L

class SearchViewModel(
    private val cardRepository: CardRepository,
    collectionRepository: CollectionRepository,
) : ViewModel() {

    private val internalState = MutableStateFlow(InternalState())
    private var searchJob: Job? = null

    val uiState: StateFlow<SearchUiState> = combine(
        internalState,
        collectionRepository.observeOwnedCardIds(),
    ) { state, ownedIds ->
        SearchUiState(
            query = state.query,
            ownershipFilter = state.ownershipFilter,
            sortDescending = state.sortDescending,
            isLoading = state.isLoading,
            results = applyOwnershipFilter(state.rawResults, state.ownershipFilter, ownedIds),
            error = state.error,
            hasSearched = state.hasSearched,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onQueryChange(query: String) {
        internalState.update { it.copy(query = query) }
        scheduleSearch(debounce = true)
    }

    /** Purely a local re-filter of already-fetched results -- no network call needed. */
    fun onOwnershipFilterSelected(filter: OwnershipFilter) {
        internalState.update {
            it.copy(ownershipFilter = if (it.ownershipFilter == filter) OwnershipFilter.ALL else filter)
        }
    }

    fun toggleSortOrder() {
        internalState.update { it.copy(sortDescending = !it.sortDescending) }
        scheduleSearch(debounce = false)
    }

    private fun scheduleSearch(debounce: Boolean) {
        searchJob?.cancel()
        val query = internalState.value.query
        if (query.isBlank()) {
            internalState.update {
                it.copy(rawResults = emptyList(), isLoading = false, hasSearched = false, error = null)
            }
            return
        }
        searchJob = viewModelScope.launch {
            if (debounce) delay(DEBOUNCE_MILLIS)
            runSearch()
        }
    }

    private suspend fun runSearch() {
        val state = internalState.value
        internalState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            cardRepository.searchByName(name = state.query, sortDescending = state.sortDescending)
        }
            .onSuccess { cards ->
                internalState.update { it.copy(isLoading = false, rawResults = cards, hasSearched = true) }
            }
            .onFailure { throwable ->
                internalState.update {
                    it.copy(isLoading = false, hasSearched = true, error = throwable.message ?: "Search failed")
                }
            }
    }

    private fun applyOwnershipFilter(
        cards: List<Card>,
        filter: OwnershipFilter,
        ownedIds: Set<String>,
    ): List<Card> = when (filter) {
        OwnershipFilter.ALL -> cards
        OwnershipFilter.OWNED -> cards.filter { it.id in ownedIds }
        OwnershipFilter.NOT_OWNED -> cards.filter { it.id !in ownedIds }
    }
}
