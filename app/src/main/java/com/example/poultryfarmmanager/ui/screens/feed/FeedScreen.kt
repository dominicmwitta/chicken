package com.example.poultryfarmmanager.ui.screens.feed

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
import com.example.poultryfarmmanager.domain.model.Feed
import com.example.poultryfarmmanager.ui.components.EmptyStateMessage
import com.example.poultryfarmmanager.ui.components.KukuCard
import com.example.poultryfarmmanager.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedScreen(showAdd: Boolean = false, viewModel: FeedViewModel = hiltViewModel()) {
    val s = LocalAppStrings.current
    LaunchedEffect(showAdd) { if (showAdd) viewModel.showAddDialog() }
    val feed                 by viewModel.feed.collectAsState()
    val lowStockFeed          = feed.filter { it.isLowStock }
    val showAddDialog        by viewModel.showAddDialog.collectAsState()
    val editingFeed          by viewModel.editingFeed.collectAsState()
    val showConsumptionDialog by viewModel.showConsumptionDialog.collectAsState()
    val selectedFeed         by viewModel.selectedFeed.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(s.addFeedLabel, fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { paddingValues ->
        if (feed.isEmpty()) {
            EmptyStateMessage(s.noFeed, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(s.feedManagementTitle, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                if (lowStockFeed.isNotEmpty()) {
                    item { LowStockAlert(count = lowStockFeed.size) }
                }
                items(feed, key = { it.id }) { f ->
                    FeedCard(feed = f, onEdit = { viewModel.showEditDialog(f) },
                        onConsume = { viewModel.showConsumptionDialog(f) },
                        onDelete = { viewModel.deleteFeed(f) })
                }
            }
        }

        if (showAddDialog) {
            AddEditFeedDialog(
                feed = editingFeed,
                onDismiss = { viewModel.hideAddDialog() },
                onSave = { name, qty, threshold, price ->
                    if (editingFeed != null) viewModel.updateFeed(editingFeed!!, name, qty, threshold, price)
                    else viewModel.addFeed(name, qty, threshold, price)
                }
            )
        }

        if (showConsumptionDialog && selectedFeed != null) {
            AddConsumptionDialog(
                feedName = selectedFeed!!.name,
                onDismiss = { viewModel.hideConsumptionDialog() },
                onSave = { qty -> viewModel.consumeFeed(selectedFeed!!, qty) }
            )
        }
    }
}

@Composable
private fun LowStockAlert(count: Int) {
    val s = LocalAppStrings.current
    val message = if (count == 1) s.feedLowSingular else s.feedLowPlural.format(count)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        Text(message, color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FeedCard(feed: Feed, onEdit: () -> Unit, onConsume: () -> Unit, onDelete: () -> Unit) {
    val s = LocalAppStrings.current
    val isLowStock = feed.isLowStock
    val accentColor = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val fillFraction = (feed.quantityKg / (feed.lowStockThreshold * 3)).coerceIn(0.0, 1.0).toFloat()

    KukuCard(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = if (isLowStock) 0.5f else 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(feed.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("${"%.1f".format(feed.quantityKg)} kg",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                        color = accentColor)
                }
                Row {
                    IconButton(onClick = onConsume) {
                        Icon(Icons.Default.Remove, s.useFeed, tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, s.edit, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, s.delete, tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxHeight()
                    .fillMaxWidth(fillFraction)
                    .clip(RoundedCornerShape(3.dp))
                    .background(accentColor))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${s.lastRestocked} ${formatDate(feed.lastRestocked)}",
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isLowStock)
                    Text(s.lowStockBadge, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddEditFeedDialog(feed: Feed?, onDismiss: () -> Unit, onSave: (String, Double, Double, Double) -> Unit) {
    val s = LocalAppStrings.current
    var name      by remember { mutableStateOf(feed?.name ?: "") }
    var quantity  by remember { mutableStateOf(feed?.quantityKg?.toString() ?: "") }
    var threshold by remember { mutableStateOf(feed?.lowStockThreshold?.toString() ?: "10") }
    var cost      by remember { mutableStateOf(feed?.cost?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(if (feed != null) "${s.edit} ${s.navFeed}" else s.addFeedLabel, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(s.feedType) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = quantity, onValueChange = { quantity = it },
                    label = { Text(s.quantityKg) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = cost, onValueChange = { cost = it },
                    label = { Text(s.totalCost) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = threshold, onValueChange = { threshold = it },
                    label = { Text(s.lowStockAlertKg) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty    = quantity.toDoubleOrNull() ?: 0.0
                    val thresh = threshold.toDoubleOrNull() ?: 10.0
                    val c      = cost.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && qty > 0) onSave(name, qty, thresh, c)
                },
                enabled = name.isNotBlank() && (quantity.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(10.dp)
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

@Composable
private fun AddConsumptionDialog(feedName: String, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    val s = LocalAppStrings.current
    var quantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(s.useFeed, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.howMuchUsed.format(feedName), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(value = quantity, onValueChange = { quantity = it },
                    label = { Text(s.quantityKg) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { val qty = quantity.toDoubleOrNull() ?: 0.0; if (qty > 0) onSave(qty) },
                enabled = (quantity.toDoubleOrNull() ?: 0.0) > 0,
                shape = RoundedCornerShape(10.dp)
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
