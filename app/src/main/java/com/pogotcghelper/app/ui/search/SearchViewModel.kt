package com.pogotcghelper.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CardRepository
import com.pogotcghelper.app.domain.model.Card
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pokémon TCG energy types, for the search filter row. */
val POKEMON_TYPES = listOf(
    "Colorless", "Darkness", "Dragon", "Fairy", "Fighting",
    "Fire", "Grass", "Lightning", "Metal", "Psychic", "Water",
)

data class SearchUiState(
    val query: String = "",
    val selectedType: String? = null,
    val sortDescending: Boolean = false,
    val isLoading: Boolean = false,
    val results: List<Card> = emptyList(),
    val error: String? = null,
    val hasSearched: Boolean = false,
)

private const val DEBOUNCE_MILLIS = 400L

class SearchViewModel(private val cardRepository: CardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        scheduleSearch(debounce = true)
    }

    fun onTypeSelected(type: String?) {
        _uiState.update { it.copy(selectedType = if (it.selectedType == type) null else type) }
        scheduleSearch(debounce = false)
    }

    fun toggleSortOrder() {
        _uiState.update { it.copy(sortDescending = !it.sortDescending) }
        scheduleSearch(debounce = false)
    }

    private fun scheduleSearch(debounce: Boolean) {
        searchJob?.cancel()
        val query = _uiState.value.query
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false, hasSearched = false, error = null) }
            return
        }
        searchJob = viewModelScope.launch {
            if (debounce) delay(DEBOUNCE_MILLIS)
            runSearch()
        }
    }

    private suspend fun runSearch() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            cardRepository.searchByName(
                name = state.query,
                type = state.selectedType,
                sortDescending = state.sortDescending,
            )
        }
            .onSuccess { cards ->
                _uiState.update { it.copy(isLoading = false, results = cards, hasSearched = true) }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(isLoading = false, hasSearched = true, error = throwable.message ?: "Search failed")
                }
            }
    }
}
