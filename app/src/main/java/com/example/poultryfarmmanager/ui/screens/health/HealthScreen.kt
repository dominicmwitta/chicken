package com.example.poultryfarmmanager.ui.screens.health

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poultryfarmmanager.domain.model.HealthRecord
import com.example.poultryfarmmanager.ui.components.EmptyStateMessage
import com.example.poultryfarmmanager.ui.components.KukuCard
import com.example.poultryfarmmanager.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

// Internal English keys — never change; used for DB storage and color mapping
private val RECORD_TYPE_KEYS = listOf("Vaccine", "Medication", "Dewormer", "Supplement", "Other")

private fun typeColor(type: String, primary: Color, secondary: Color, error: Color, tertiary: Color): Color = when (type) {
    "Vaccine"    -> primary
    "Medication" -> error
    "Dewormer"   -> secondary
    "Supplement" -> tertiary
    else         -> primary.copy(alpha = 0.6f)
}

@Composable
private fun localizedTypeName(type: String): String {
    val s = LocalAppStrings.current
    return when (type) {
        "Vaccine"    -> s.typeVaccine
        "Medication" -> s.typeMedication
        "Dewormer"   -> s.typeDewormer
        "Supplement" -> s.typeSupplement
        else         -> s.typeOther
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(showAdd: Boolean = false, viewModel: HealthViewModel = hiltViewModel()) {
    val s = LocalAppStrings.current
    LaunchedEffect(showAdd) { if (showAdd) viewModel.showAddDialog() }
    val records       by viewModel.records.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingRecord by viewModel.editingRecord.collectAsState()

    val totalCost = records.sumOf { it.cost }
    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val error     = MaterialTheme.colorScheme.error
    val tertiary  = Color(0xFF8B5CF6)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(s.addRecord, fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { paddingValues ->
        if (records.isEmpty()) {
            EmptyStateMessage(s.noHealth, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(s.healthTitle, style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp))
                }
                item {
                    HealthSummaryCard(
                        totalRecords = records.size,
                        totalCost    = totalCost,
                        vaccines     = records.count { it.type == "Vaccine" },
                        medications  = records.count { it.type == "Medication" }
                    )
                }
                items(records, key = { it.id }) { record ->
                    HealthRecordCard(
                        record   = record,
                        color    = typeColor(record.type, primary, secondary, error, tertiary),
                        onEdit   = { viewModel.showEditDialog(record) },
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddEditHealthDialog(
                record    = editingRecord,
                onDismiss = { viewModel.hideDialog() },
                onSave    = { type, name, date, cost, dosage, birds, notes ->
                    if (editingRecord != null)
                        viewModel.updateRecord(editingRecord!!, type, name, date, cost, dosage, birds, notes)
                    else
                        viewModel.addRecord(type, name, date, cost, dosage, birds, notes)
                }
            )
        }
    }
}

@Composable
private fun HealthSummaryCard(totalRecords: Int, totalCost: Double, vaccines: Int, medications: Int) {
    val s = LocalAppStrings.current
    KukuCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(s.records,      totalRecords.toString(),     MaterialTheme.colorScheme.primary)
            SummaryItem(s.totalCost,    "%.0f/-".format(totalCost),  MaterialTheme.colorScheme.secondary)
            SummaryItem(s.vaccines,     vaccines.toString(),         MaterialTheme.colorScheme.primary)
            SummaryItem(s.medications,  medications.toString(),      MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}

@Composable
private fun HealthRecordCard(record: HealthRecord, color: Color, onEdit: () -> Unit, onDelete: () -> Unit) {
    val s = LocalAppStrings.current
    var showMenu by remember { mutableStateOf(false) }

    KukuCard(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(localizedTypeName(record.type), style = MaterialTheme.typography.labelSmall,
                    color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(record.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(formatDate(record.date), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (record.cost > 0)
                        Text("${"%.0f".format(record.cost)}/-", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                    if (record.birdsAffected > 0)
                        Text("${record.birdsAffected} ${s.birdsSuffix}", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (record.dosage.isNotEmpty())
                    Text("${s.dosagePrefix} ${record.dosage}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (record.notes.isNotEmpty())
                    Text(record.notes, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditHealthDialog(
    record: HealthRecord?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long, Double, String, Int, String) -> Unit
) {
    val s = LocalAppStrings.current
    val typeLabels = listOf(s.typeVaccine, s.typeMedication, s.typeDewormer, s.typeSupplement, s.typeOther)

    var selectedTypeKey by remember { mutableStateOf(record?.type ?: RECORD_TYPE_KEYS[0]) }
    var name            by remember { mutableStateOf(record?.name ?: "") }
    var date            by remember { mutableStateOf(record?.date ?: System.currentTimeMillis()) }
    var cost            by remember { mutableStateOf(record?.cost?.let { if (it > 0) "%.0f".format(it) else "" } ?: "") }
    var dosage          by remember { mutableStateOf(record?.dosage ?: "") }
    var birdsAffected   by remember { mutableStateOf(record?.birdsAffected?.toString() ?: "") }
    var notes           by remember { mutableStateOf(record?.notes ?: "") }
    var showDatePicker  by remember { mutableStateOf(false) }
    var typeExpanded    by remember { mutableStateOf(false) }

    val selectedLabel = typeLabels[RECORD_TYPE_KEYS.indexOf(selectedTypeKey).coerceAtLeast(0)]

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(if (record != null) s.editHealthRecord else s.addHealthRecord, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(s.typeLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        RECORD_TYPE_KEYS.forEachIndexed { index, key ->
                            DropdownMenuItem(
                                text = { Text(typeLabels[index]) },
                                onClick = { selectedTypeKey = key; typeExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(s.nameHint) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                OutlinedTextField(
                    value = formatDate(date), onValueChange = {}, readOnly = true,
                    label = { Text(s.date) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = { IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, null) } }
                )

                OutlinedTextField(value = cost, onValueChange = { cost = it },
                    label = { Text(s.costOptional) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                OutlinedTextField(value = dosage, onValueChange = { dosage = it },
                    label = { Text(s.dosageOptional) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                OutlinedTextField(value = birdsAffected, onValueChange = { birdsAffected = it.filter { c -> c.isDigit() } },
                    label = { Text(s.birdsTreatedOptional) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text(s.notesOptional) }, modifier = Modifier.fillMaxWidth(),
                    minLines = 2, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank())
                        onSave(selectedTypeKey, name, date, cost.toDoubleOrNull() ?: 0.0,
                            dosage, birdsAffected.toIntOrNull() ?: 0, notes)
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) { Text(s.save) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(s.cancel) } }
        ) { DatePicker(state = datePickerState) }
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
