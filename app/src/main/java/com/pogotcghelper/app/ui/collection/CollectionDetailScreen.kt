package com.pogotcghelper.app.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pogotcghelper.app.R
import com.pogotcghelper.app.domain.model.OwnedCard
import com.pogotcghelper.app.ui.common.CardArtwork
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(viewModel: CollectionDetailViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.collectionName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = stringResource(
                    R.string.collection_total_value,
                    String.format(Locale.US, "$%.2f", uiState.totalValueUsd),
                ),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp),
            )

            if (uiState.cards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.collection_empty))
                }
            } else {
                LazyColumn {
                    items(uiState.cards, key = { it.cardId }) { card ->
                        OwnedCardRow(
                            card = card,
                            onRemoveOne = { viewModel.removeOne(card.cardId) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnedCardRow(card: OwnedCard, onRemoveOne: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CardArtwork(
                imageUrl = card.imageUrl,
                contentDescription = card.name,
                modifier = Modifier.size(56.dp),
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = card.name, style = MaterialTheme.typography.bodyLarge)
                card.setName?.let { Text(text = it, style = MaterialTheme.typography.labelMedium) }
                Text(text = "Qty: ${card.quantity}")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = card.marketPrice?.let { String.format(Locale.US, "$%.2f", it * card.quantity) } ?: "—",
                modifier = Modifier.padding(end = 8.dp),
            )
            IconButton(onClick = onRemoveOne) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove_from_collection))
            }
        }
    }
}
