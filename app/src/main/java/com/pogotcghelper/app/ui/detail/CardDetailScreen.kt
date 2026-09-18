package com.pogotcghelper.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.pogotcghelper.app.domain.model.Attack
import com.pogotcghelper.app.domain.model.Card
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
                    ownedQuantity = uiState.ownedQuantity,
                    onAdd = viewModel::addToCollection,
                    onRemove = viewModel::removeFromCollection,
                )
            }
        }
    }
}

@Composable
private fun CardDetailContent(
    card: Card,
    ownedQuantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
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

        CollectionControls(ownedQuantity = ownedQuantity, onAdd = onAdd, onRemove = onRemove)

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
        PriceRow(label = stringResource(R.string.tcgplayer), price = card.tcgplayerMarketPriceUsd, symbol = "$")
        PriceRow(label = stringResource(R.string.cardmarket), price = card.cardmarketPriceEur, symbol = "€")
    }
}

@Composable
private fun CollectionControls(ownedQuantity: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onAdd) { Text(stringResource(R.string.add_to_collection)) }
        if (ownedQuantity > 0) {
            OutlinedButton(onClick = onRemove) { Text(stringResource(R.string.remove_from_collection)) }
            Text(text = "Owned: $ownedQuantity", style = MaterialTheme.typography.bodyLarge)
        }
    }
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
private fun PriceRow(label: String, price: Double?, symbol: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label)
        Text(text = price?.let { String.format(Locale.US, "%s%.2f", symbol, it) } ?: "—")
    }
}
