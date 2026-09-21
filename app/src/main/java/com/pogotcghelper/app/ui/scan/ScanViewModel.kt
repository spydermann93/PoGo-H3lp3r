package com.pogotcghelper.app.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pogotcghelper.app.data.repository.CardRepository
import com.pogotcghelper.app.domain.model.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ScanPhase { CAMERA, RECOGNIZING, REVIEW, SEARCHING, RESULTS, NO_TEXT_FOUND }

data class ScanUiState(
    val phase: ScanPhase = ScanPhase.CAMERA,
    val query: String = "",
    val recognizedLines: List<String> = emptyList(),
    val results: List<Card> = emptyList(),
    val error: String? = null,
)

/**
 * Words that show up on virtually every Pokémon card but are never the card's own name --
 * skipped when guessing which recognized line is the name. Not exhaustive, just enough to
 * avoid the most common false positives (an all-caps "BASIC" or "HP" line sitting above the
 * actual name in scan order).
 */
private val NON_NAME_WORDS = setOf(
    "HP", "BASIC", "STAGE 1", "STAGE 2", "TRAINER", "ENERGY", "ITEM", "SUPPORTER",
    "STADIUM", "POKÉMON", "POKEMON",
)

class ScanViewModel(private val cardRepository: CardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onCapturing() {
        _uiState.update { it.copy(phase = ScanPhase.RECOGNIZING, error = null) }
    }

    fun onTextRecognized(lines: List<String>) {
        _uiState.update {
            it.copy(
                phase = if (lines.isEmpty()) ScanPhase.NO_TEXT_FOUND else ScanPhase.REVIEW,
                recognizedLines = lines,
                query = guessCardName(lines),
                results = emptyList(),
            )
        }
        if (lines.isNotEmpty()) search()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun retake() {
        _uiState.value = ScanUiState()
    }

    fun search() {
        val query = _uiState.value.query
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ScanPhase.SEARCHING, error = null) }
            runCatching { cardRepository.searchByName(query) }
                .onSuccess { cards -> _uiState.update { it.copy(phase = ScanPhase.RESULTS, results = cards) } }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(phase = ScanPhase.REVIEW, error = throwable.message ?: "Search failed")
                    }
                }
        }
    }
}

/** Best-effort guess at which recognized line is the card's name -- see [NON_NAME_WORDS]. */
private fun guessCardName(lines: List<String>): String {
    val candidate = lines.firstOrNull { line ->
        val upper = line.uppercase()
        line.length in 3..24 &&
            line.any { it.isLetter() } &&
            upper !in NON_NAME_WORDS &&
            !line.all { it.isDigit() || it == '/' }
    }
    return candidate ?: lines.firstOrNull().orEmpty()
}
