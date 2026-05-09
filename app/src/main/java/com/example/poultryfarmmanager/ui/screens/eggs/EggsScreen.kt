package com.example.poultryfarmmanager.ui.screens.eggs

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poultryfarmmanager.domain.model.EggProduction
import com.example.poultryfarmmanager.ui.components.EmptyStateMessage
import com.example.poultryfarmmanager.ui.components.KukuCard
import com.example.poultryfarmmanager.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EggsScreen(showAdd: Boolean = false, viewModel: EggsViewModel = hiltViewModel()) {
    val s = LocalAppStrings.current
    LaunchedEffect(showAdd) { if (showAdd) viewModel.showAddDialog() }
    val eggs            by viewModel.eggs.collectAsState()
    val todayProduction by viewModel.todayProduction.collectAsState()
    val todayTotal      by viewModel.todayTotalEggs.collectAsState()
    val showAddDialog   by viewModel.showAddDialog.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor   = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(s.logEggs, fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { paddingValues ->
        if (eggs.isEmpty()) {
            EmptyStateMessage(s.noEggs, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(s.eggProductionTitle, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                item { TodaySummaryCard(todayProduction, todayTotal) }
                items(eggs, key = { it.id }) { prod ->
                    EggProductionCard(production = prod, onDelete = { viewModel.deleteEgg(prod) })
                }
            }
        }

        if (showAddDialog) {
            AddEggProductionDialog(
                onDismiss = { viewModel.hideDialog() },
                onSave = { total, sold, price, consumed, notes ->
                    viewModel.addEgg(total, sold, price, consumed, notes)
                }
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(today: EggProduction?, total: Int) {
    val s = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                )
            )
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(s.todayProduction, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            Text("$total ${s.eggsUnit}", style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            if (today != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    EggStat(s.birdSold,   today.eggsSold)
                    EggStat(s.consumed,   today.eggsConsumed)
                    EggStat(s.remaining,  today.remaining)
                    EggStat(s.revenue,    today.revenue)
                }
            }
        }
    }
}

@Composable
private fun EggStat(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
private fun EggStat(label: String, value: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("%.1f".format(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
    }
}

@Composable
private fun EggProductionCard(production: EggProduction, onDelete: () -> Unit) {
    val s = LocalAppStrings.current
    KukuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(formatDate(production.date), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("${production.totalEggs} ${s.eggsUnit}", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatPill("${s.birdSold} ${production.eggsSold}")
                StatPill("${s.consumed} ${production.eggsConsumed}")
                StatPill("${s.remaining} ${production.remaining}")
                StatPill("${"%.0f".format(production.revenue)}/-")
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(s.delete, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun StatPill(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun AddEggProductionDialog(onDismiss: () -> Unit, onSave: (Int, Int, Double, Int, String) -> Unit) {
    val s = LocalAppStrings.current
    var total    by remember { mutableStateOf("") }
    var sold     by remember { mutableStateOf("0") }
    var price    by remember { mutableStateOf("") }
    var consumed by remember { mutableStateOf("0") }
    var notes    by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(s.logEggProduction, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = total, onValueChange = { total = it.filter { c -> c.isDigit() } },
                    label = { Text(s.totalEggsCollected) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = sold, onValueChange = { sold = it.filter { c -> c.isDigit() } },
                    label = { Text(s.eggsSold) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text(s.pricePerEgg) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = consumed, onValueChange = { consumed = it.filter { c -> c.isDigit() } },
                    label = { Text(s.eggsConsumed) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text(s.notesOptional) }, modifier = Modifier.fillMaxWidth(),
                    minLines = 2, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(total.toIntOrNull() ?: 0, sold.toIntOrNull() ?: 0,
                    price.toDoubleOrNull() ?: 0.0, consumed.toIntOrNull() ?: 0, notes) },
                enabled = (total.toIntOrNull() ?: 0) > 0,
                shape = RoundedCornerShape(10.dp)
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
