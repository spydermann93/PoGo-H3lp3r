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
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pogotcghelper.app.R
import com.pogotcghelper.app.domain.model.Collection
import com.pogotcghelper.app.domain.model.OwnedCard
import com.pogotcghelper.app.ui.common.CardArtwork
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(viewModel: CollectionDetailViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    var cardToAdd by remember { mutableStateOf<OwnedCard?>(null) }

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
            val headerText = if (uiState.isTracker) {
                stringResource(
                    R.string.tracker_progress,
                    uiState.cards.count { it.quantity > 0 },
                    uiState.cards.size,
                )
            } else {
                stringResource(R.string.collection_total_value, String.format(Locale.US, "$%.2f", uiState.totalValueUsd))
            }
            Text(
                text = headerText,
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
                            isTracker = uiState.isTracker,
                            onToggleTracked = { viewModel.toggleTracked(card.cardId) },
                            onRemoveOne = { viewModel.removeOne(card.cardId) },
                            onAddToCollection = { cardToAdd = card },
                        )
                    }
                }
            }
        }
    }

    cardToAdd?.let { card ->
        AddToCollectionDialog(
            cardName = card.name,
            collections = uiState.realCollections,
            onDismiss = { cardToAdd = null },
            onSelect = { collectionId ->
                viewModel.addToRealCollection(collectionId, card)
                cardToAdd = null
            },
            onCreateAndAdd = { name ->
                viewModel.createRealCollectionAndAdd(name, card)
                cardToAdd = null
            },
        )
    }
}

/**
 * Ownership/collected state is shown three ways at once -- desaturated art, a
 * check-vs-add icon, and a text label -- so it doesn't rely on color alone (e.g. for
 * colorblind viewers).
 */
@Composable
private fun OwnedCardGridItem(
    card: OwnedCard,
    isTracker: Boolean,
    onToggleTracked: () -> Unit,
    onRemoveOne: () -> Unit,
    onAddToCollection: () -> Unit,
) {
    val isChecked = card.quantity > 0
    val accentColor = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    // A tracker's checkmark can be tapped either way (it's just a checklist tick); a real
    // collection's can only go from not-owned to owned, never reset an owned row to zero.
    val badgeClickable = isTracker || !isChecked

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
                grayscale = !isChecked,
            )
            Icon(
                imageVector = if (isChecked) Icons.Filled.CheckCircle else Icons.Filled.AddCircle,
                contentDescription = stringResource(
                    if (isChecked) R.string.collected else R.string.mark_collected
                ),
                tint = accentColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .size(28.dp)
                    .let { if (badgeClickable) it.clickable(onClick = onToggleTracked) else it },
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
                    text = if (isTracker) {
                        stringResource(if (isChecked) R.string.collected else R.string.not_collected_yet)
                    } else if (isChecked) {
                        "Qty: ${card.quantity}"
                    } else {
                        stringResource(R.string.not_owned_yet)
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
                if (!isTracker && isChecked) {
                    Text(
                        text = card.marketPrice?.let { String.format(Locale.US, "$%.2f", it * card.quantity) } ?: "—",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            Row {
                if (isTracker) {
                    IconButton(onClick = onAddToCollection, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.LibraryAdd,
                            contentDescription = stringResource(R.string.add_to_my_collection),
                            modifier = Modifier.size(18.dp),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToCollectionDialog(
    cardName: String,
    collections: List<Collection>,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
) {
    var newCollectionName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_to_collection_title, cardName)) },
        text = {
            Column {
                collections.forEach { collection ->
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(collection.id) }
                            .padding(vertical = 12.dp),
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                OutlinedTextField(
                    value = newCollectionName,
                    onValueChange = { newCollectionName = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.collection_name_placeholder)) },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreateAndAdd(newCollectionName) },
                enabled = newCollectionName.isNotBlank(),
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
