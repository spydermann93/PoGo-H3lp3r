package com.pogotcghelper.app.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import com.pogotcghelper.app.domain.model.TcgSet
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsListScreen(
    viewModel: CollectionsListViewModel,
    onCollectionClick: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddOptions by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var pickingSet by remember { mutableStateOf(false) }
    var setToImport by remember { mutableStateOf<TcgSet?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_collection)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddOptions = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.new_collection))
            }
        },
    ) { padding ->
        if (uiState.collections.isEmpty()) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.collections_empty))
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(uiState.collections, key = { it.id }) { collection ->
                    CollectionRow(
                        collection = collection,
                        onClick = { onCollectionClick(collection.id) },
                        onDelete = { viewModel.deleteCollection(collection.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddOptions) {
        AddOptionsDialog(
            onDismiss = { showAddOptions = false },
            onNewCollection = {
                showAddOptions = false
                showCreateDialog = true
            },
            onImportSet = {
                showAddOptions = false
                pickingSet = true
            },
        )
    }

    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createCollection(name) { newId -> onCollectionClick(newId) }
                showCreateDialog = false
            },
        )
    }

    if (pickingSet) {
        SetPickerDialog(
            sets = uiState.availableSets,
            onDismiss = { pickingSet = false },
            onSetSelected = { set ->
                pickingSet = false
                setToImport = set
            },
        )
    }

    setToImport?.let { set ->
        ImportSetDialog(
            setName = set.name,
            trackers = uiState.collections.filter { it.isTracker },
            onDismiss = { setToImport = null },
            onConfirm = { collectionId, newCollectionName, baseSetOnly ->
                viewModel.importSet(set.id, baseSetOnly, collectionId, newCollectionName) { id ->
                    setToImport = null
                    onCollectionClick(id)
                }
            },
        )
    }
}

@Composable
private fun CollectionRow(collection: Collection, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = collection.name, style = MaterialTheme.typography.bodyLarge)
            if (collection.isTracker) {
                Text(
                    text = stringResource(R.string.tracker_progress, collection.cardCount, collection.totalTracked),
                    style = MaterialTheme.typography.labelMedium,
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.collection_card_count,
                        collection.cardCount,
                        String.format(Locale.US, "$%.2f", collection.totalValueUsd),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_collection))
        }
    }
}

@Composable
private fun AddOptionsDialog(
    onDismiss: () -> Unit,
    onNewCollection: () -> Unit,
    onImportSet: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_options_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.new_collection),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onNewCollection).padding(vertical = 12.dp),
                )
                Text(
                    text = stringResource(R.string.import_set),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onImportSet).padding(vertical = 12.dp),
                )
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun CreateCollectionDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_collection)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.collection_name_placeholder)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun SetPickerDialog(
    sets: List<TcgSet>,
    onDismiss: () -> Unit,
    onSetSelected: (TcgSet) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(sets, query) {
        if (query.isBlank()) sets else sets.filter { it.name.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pick_set_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.pick_set_search_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp).padding(top = 8.dp)) {
                    items(filtered, key = { it.id }) { set ->
                        Text(
                            text = set.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSetSelected(set) }
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/**
 * Importing a set always creates or adds to a tracker -- a completion checklist, never
 * your real inventory -- so only existing trackers are offered as a target, never a
 * regular collection. See [CollectionRepository.addSet] for why.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportSetDialog(
    setName: String,
    trackers: List<Collection>,
    onDismiss: () -> Unit,
    onConfirm: (collectionId: Long?, newCollectionName: String?, baseSetOnly: Boolean) -> Unit,
) {
    var selectedCollectionId by remember { mutableStateOf(trackers.firstOrNull()?.id) }
    var creatingNew by remember { mutableStateOf(trackers.isEmpty()) }
    var newCollectionName by remember { mutableStateOf("$setName Master Set Tracker") }
    var nameManuallyEdited by remember { mutableStateOf(false) }
    var baseSetOnly by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_set_title, setName)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.import_set_explainer),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !baseSetOnly,
                        onClick = {
                            baseSetOnly = false
                            if (!nameManuallyEdited) newCollectionName = "$setName Master Set Tracker"
                        },
                        label = { Text(stringResource(R.string.import_scope_master)) },
                    )
                    FilterChip(
                        selected = baseSetOnly,
                        onClick = {
                            baseSetOnly = true
                            if (!nameManuallyEdited) newCollectionName = "$setName Base Set Tracker"
                        },
                        label = { Text(stringResource(R.string.import_scope_base)) },
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Column(modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                    trackers.forEach { tracker ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    creatingNew = false
                                    selectedCollectionId = tracker.id
                                },
                        ) {
                            RadioButton(
                                selected = !creatingNew && selectedCollectionId == tracker.id,
                                onClick = {
                                    creatingNew = false
                                    selectedCollectionId = tracker.id
                                },
                            )
                            Text(tracker.name)
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { creatingNew = true },
                    ) {
                        RadioButton(selected = creatingNew, onClick = { creatingNew = true })
                        Text(stringResource(R.string.new_tracker))
                    }
                    if (creatingNew) {
                        OutlinedTextField(
                            value = newCollectionName,
                            onValueChange = {
                                newCollectionName = it
                                nameManuallyEdited = true
                            },
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.collection_name_placeholder)) },
                            modifier = Modifier.padding(start = 40.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            val enabled = if (creatingNew) newCollectionName.isNotBlank() else selectedCollectionId != null
            TextButton(
                enabled = enabled,
                onClick = {
                    onConfirm(
                        if (creatingNew) null else selectedCollectionId,
                        if (creatingNew) newCollectionName else null,
                        baseSetOnly,
                    )
                },
            ) { Text(stringResource(R.string.import_action)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
