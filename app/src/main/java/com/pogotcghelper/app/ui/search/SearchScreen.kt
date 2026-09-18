package com.pogotcghelper.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pogotcghelper.app.R
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.ui.common.CardArtwork

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onCardClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_search)) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            )

            FilterAndSortRow(
                ownershipFilter = uiState.ownershipFilter,
                sortDescending = uiState.sortDescending,
                onOwnershipFilterSelected = viewModel::onOwnershipFilterSelected,
                onSortToggle = viewModel::toggleSortOrder,
            )

            when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> MessageState(uiState.error ?: stringResource(R.string.error_generic))
                uiState.hasSearched && uiState.results.isEmpty() -> MessageState(stringResource(R.string.search_no_results))
                !uiState.hasSearched -> MessageState(stringResource(R.string.search_empty))
                else -> CardGrid(cards = uiState.results, onCardClick = onCardClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterAndSortRow(
    ownershipFilter: OwnershipFilter,
    sortDescending: Boolean,
    onOwnershipFilterSelected: (OwnershipFilter) -> Unit,
    onSortToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onSortToggle) {
            Icon(
                imageVector = if (sortDescending) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                contentDescription = stringResource(R.string.sort_by_name),
            )
        }
        FilterChip(
            selected = ownershipFilter == OwnershipFilter.OWNED,
            onClick = { onOwnershipFilterSelected(OwnershipFilter.OWNED) },
            label = { Text(stringResource(R.string.filter_owned)) },
        )
        FilterChip(
            selected = ownershipFilter == OwnershipFilter.NOT_OWNED,
            onClick = { onOwnershipFilterSelected(OwnershipFilter.NOT_OWNED) },
            label = { Text(stringResource(R.string.filter_not_owned)) },
        )
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MessageState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CardGrid(cards: List<Card>, onCardClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(cards, key = { it.id }) { card ->
            CardGridItem(card = card, onClick = { onCardClick(card.id) })
        }
    }
}

@Composable
private fun CardGridItem(card: Card, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable(onClick = onClick),
    ) {
        CardArtwork(
            imageUrl = card.smallImageUrl,
            contentDescription = card.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(0.72f),
        )
        Text(
            text = card.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
        card.setName?.let {
            Text(text = it, style = MaterialTheme.typography.labelMedium)
        }
    }
}
