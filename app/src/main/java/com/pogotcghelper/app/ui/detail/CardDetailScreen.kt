package com.pogotcghelper.app.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pogotcghelper.app.R
import com.pogotcghelper.app.domain.model.Attack
import com.pogotcghelper.app.domain.model.Card
import com.pogotcghelper.app.domain.model.Collection
import com.pogotcghelper.app.domain.model.TypeValue
import com.pogotcghelper.app.ui.common.CardArtwork
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    viewModel: CardDetailViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.card?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    text = uiState.error ?: stringResource(R.string.error_generic),
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                uiState.card != null -> CardDetailContent(
                    card = uiState.card!!,
                    collections = uiState.collections,
                    quantitiesByCollection = uiState.quantitiesByCollection,
                    onAdd = viewModel::addToCollection,
                    onRemove = viewModel::removeFromCollection,
                    onCreateAndAdd = viewModel::createCollectionAndAdd,
                )
            }
        }
    }
}

@Composable
private fun CardDetailContent(
    card: Card,
    collections: List<Collection>,
    quantitiesByCollection: Map<Long, Int>,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        CardArtwork(
            imageUrl = card.largeImageUrl,
            contentDescription = card.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(0.72f),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = card.name, style = MaterialTheme.typography.titleLarge)
                Text(text = listOfNotNull(card.setName, card.number).joinToString(" · "))
            }
            card.hp?.let { Text(text = "HP $it", style = MaterialTheme.typography.titleLarge) }
        }

        CollectionSection(
            collections = collections,
            quantitiesByCollection = quantitiesByCollection,
            onAdd = onAdd,
            onRemove = onRemove,
            onCreateAndAdd = onCreateAndAdd,
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        if (card.attacks.isNotEmpty()) {
            SectionTitle(stringResource(R.string.attacks))
            card.attacks.forEach { AttackRow(it) }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }

        if (card.weaknesses.isNotEmpty()) {
            SectionTitle(stringResource(R.string.weaknesses))
            TypeValueRow(card.weaknesses)
        }

        if (card.resistances.isNotEmpty()) {
            SectionTitle(stringResource(R.string.resistances))
            TypeValueRow(card.resistances)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        SectionTitle(stringResource(R.string.market_prices))
        PriceRow(
            label = stringResource(R.string.tcgplayer),
            price = card.tcgplayerMarketPriceUsd,
            symbol = "$",
            url = card.tcgplayerUrl,
        )
        PriceRow(
            label = stringResource(R.string.cardmarket),
            price = card.cardmarketPriceEur,
            symbol = "€",
            url = card.cardmarketUrl,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionSection(
    collections: List<Collection>,
    quantitiesByCollection: Map<Long, Int>,
    onAdd: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Button(onClick = { showPicker = true }) { Text(stringResource(R.string.add_to_collection)) }

        val ownedIn = collections.filter { (quantitiesByCollection[it.id] ?: 0) > 0 }
        ownedIn.forEach { collection ->
            val quantity = quantitiesByCollection[collection.id] ?: 0
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "${collection.name} · Qty: $quantity")
                IconButton(onClick = { onRemove(collection.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.remove_from_collection))
                }
            }
        }
    }

    if (showPicker) {
        CollectionPickerDialog(
            collections = collections,
            onDismiss = { showPicker = false },
            onSelect = { id -> onAdd(id); showPicker = false },
            onCreateAndAdd = { name -> onCreateAndAdd(name); showPicker = false },
        )
    }
}

@Composable
private fun CollectionPickerDialog(
    collections: List<Collection>,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit,
    onCreateAndAdd: (String) -> Unit,
) {
    var newCollectionName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.choose_collection)) },
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

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun AttackRow(attack: Attack) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = attack.name, style = MaterialTheme.typography.bodyLarge)
            attack.damage?.let { Text(text = it) }
        }
        attack.text?.let { Text(text = it, style = MaterialTheme.typography.labelMedium) }
    }
}

@Composable
private fun TypeValueRow(values: List<TypeValue>) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        values.forEach { Text(text = "${it.type} ${it.value}") }
    }
}

@Composable
private fun PriceRow(label: String, price: Double?, symbol: String, url: String?) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (url != null) {
                    modifier.clickable {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    }
                } else {
                    modifier
                }
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = price?.let { String.format(Locale.US, "%s%.2f", symbol, it) } ?: "—")
            if (url != null) {
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = stringResource(R.string.view_sales),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
