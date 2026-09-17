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

data class SearchUiState(
    val query: String = "",
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
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false, hasSearched = false, error = null) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            runSearch(query)
        }
    }

    private suspend fun runSearch(query: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching { cardRepository.searchByName(query) }
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
