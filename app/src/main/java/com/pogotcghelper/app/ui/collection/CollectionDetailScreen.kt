package com.pogotcghelper.app.ui.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.cards, key = { it.cardId }) { card ->
                        OwnedCardGridItem(
                            card = card,
                            onMarkOwned = { viewModel.markOwned(card.cardId) },
                            onRemoveOne = { viewModel.removeOne(card.cardId) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Ownership is shown three ways at once -- desaturated art, a check-vs-add icon, and a
 * text label -- so the state doesn't rely on color alone (e.g. for colorblind viewers).
 */
@Composable
private fun OwnedCardGridItem(card: OwnedCard, onMarkOwned: () -> Unit, onRemoveOne: () -> Unit) {
    val isOwned = card.quantity > 0
    val accentColor = if (isOwned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = accentColor, shape = RoundedCornerShape(8.dp))
            .padding(8.dp),
    ) {
        Box {
            CardArtwork(
                imageUrl = card.imageUrl,
                contentDescription = card.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(0.72f),
                grayscale = !isOwned,
            )
            Icon(
                imageVector = if (isOwned) Icons.Filled.CheckCircle else Icons.Filled.AddCircle,
                contentDescription = if (isOwned) {
                    stringResource(R.string.filter_owned)
                } else {
                    stringResource(R.string.mark_owned)
                },
                tint = accentColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .size(28.dp)
                    .let { if (isOwned) it else it.clickable(onClick = onMarkOwned) },
            )
        }

        Text(
            text = card.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            modifier = Modifier.padding(top = 6.dp),
        )
        card.setName?.let { Text(text = it, style = MaterialTheme.typography.labelMedium, maxLines = 1) }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = if (isOwned) "Qty: ${card.quantity}" else stringResource(R.string.not_owned_yet),
                    style = MaterialTheme.typography.labelMedium,
                )
                if (isOwned) {
                    Text(
                        text = card.marketPrice?.let { String.format(Locale.US, "$%.2f", it * card.quantity) } ?: "—",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            IconButton(onClick = onRemoveOne, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.remove_from_collection),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
