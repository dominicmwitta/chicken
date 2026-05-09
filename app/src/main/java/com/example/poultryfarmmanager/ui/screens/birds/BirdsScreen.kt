package com.example.poultryfarmmanager.ui.screens.birds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poultryfarmmanager.domain.model.Bird
import com.example.poultryfarmmanager.ui.components.EmptyStateMessage
import com.example.poultryfarmmanager.ui.components.KukuCard
import com.example.poultryfarmmanager.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BirdsScreen(showAdd: Boolean = false, viewModel: BirdsViewModel = hiltViewModel()) {
    val s = LocalAppStrings.current
    LaunchedEffect(showAdd) { if (showAdd) viewModel.showAddDialog() }
    val birds         by viewModel.birds.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingBird   by viewModel.editingBird.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(s.addBatch, fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { paddingValues ->
        if (birds.isEmpty()) {
            EmptyStateMessage(s.noBirds, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(s.birdInventoryTitle, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                items(birds, key = { it.id }) { bird ->
                    BirdCard(bird = bird, onEdit = { viewModel.showEditDialog(bird) },
                        onDelete = { viewModel.deleteBird(bird) })
                }
            }
        }

        if (showAddDialog) {
            AddEditBirdDialog(
                bird = editingBird,
                onDismiss = { viewModel.hideDialog() },
                onSave = { breed, quantity, sold, price, slaughtered, notes ->
                    if (editingBird != null) viewModel.updateBird(breed, quantity, sold, price, slaughtered, notes)
                    else viewModel.addBird(breed, quantity, sold, price, slaughtered, notes)
                }
            )
        }
    }
}

@Composable
private fun BirdCard(bird: Bird, onEdit: () -> Unit, onDelete: () -> Unit) {
    val s = LocalAppStrings.current
    var showMenu by remember { mutableStateOf(false) }

    KukuCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .width(4.dp).height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(bird.breed, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip("${bird.remaining} ${s.birdLeft}", MaterialTheme.colorScheme.primary)
                    InfoChip("${bird.sold} ${s.birdSold}", MaterialTheme.colorScheme.secondary)
                    if (bird.slaughtered > 0)
                        InfoChip("${bird.slaughtered} ${s.birdSlaughtered}", MaterialTheme.colorScheme.error)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${s.totalPrefix} ${bird.quantity}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (bird.revenue > 0)
                        Text("${s.revenuePrefix} ${"%.0f".format(bird.revenue)}/-",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary)
                }
                Text(formatDate(bird.dateAcquired), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text(s.edit) }, onClick = { showMenu = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem(
                        text = { Text(s.delete, color = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
        if (bird.notes.isNotEmpty()) {
            Text(bird.notes, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 34.dp, end = 16.dp, bottom = 12.dp))
        }
    }
}

@Composable
private fun InfoChip(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Medium)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditBirdDialog(bird: Bird?, onDismiss: () -> Unit, onSave: (String, Int, Int, Double, Int, String) -> Unit) {
    val s = LocalAppStrings.current
    var breed       by remember { mutableStateOf(bird?.breed ?: "") }
    var quantity    by remember { mutableStateOf(bird?.quantity?.toString() ?: "") }
    var sold        by remember { mutableStateOf(bird?.sold?.toString() ?: "0") }
    var price       by remember { mutableStateOf(bird?.pricePerBird?.toString() ?: "") }
    var slaughtered by remember { mutableStateOf(bird?.slaughtered?.toString() ?: "0") }
    var notes       by remember { mutableStateOf(bird?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(if (bird != null) s.editBirdBatch else s.addBirdBatch, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = breed, onValueChange = { breed = it },
                    label = { Text(s.breed) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text(s.totalQuantity) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = sold, onValueChange = { sold = it.filter { c -> c.isDigit() } },
                    label = { Text(s.birdSold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text(s.pricePerBird) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = slaughtered, onValueChange = { slaughtered = it.filter { c -> c.isDigit() } },
                    label = { Text(s.birdSlaughtered) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text(s.notesOptional) }, modifier = Modifier.fillMaxWidth(),
                    minLines = 2, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (breed.isNotBlank() && qty > 0)
                        onSave(breed, qty, sold.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0,
                            slaughtered.toIntOrNull() ?: 0, notes)
                },
                enabled = breed.isNotBlank() && (quantity.toIntOrNull() ?: 0) > 0,
                shape = RoundedCornerShape(10.dp)
            ) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        }
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
