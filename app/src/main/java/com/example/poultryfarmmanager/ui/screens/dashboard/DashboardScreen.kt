package com.example.poultryfarmmanager.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.poultryfarmmanager.data.sync.SyncState
import com.example.poultryfarmmanager.ui.components.KukuCard
import com.example.poultryfarmmanager.ui.components.SectionLabel
import com.example.poultryfarmmanager.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

private val RANGE_OPTIONS = listOf(7, 14, 30, 90)

@Composable
fun DashboardScreen(
    onNavigateTo: (String) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    languageViewModel: LanguageViewModel = hiltViewModel()
) {
    val s            = LocalAppStrings.current
    val totalBirds   by viewModel.totalBirds.collectAsState()
    val todayEggs    by viewModel.todayEggs.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val lowStockCount by viewModel.lowStockFeedCount.collectAsState()
    val syncState    by viewModel.syncState.collectAsState()
    val syncMessage  by viewModel.syncMessage.collectAsState()
    val analytics    by viewModel.analytics.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val language     by languageViewModel.language.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(s.dashboardTitle, style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
            // Language toggle
            TextButton(
                onClick = { languageViewModel.toggle() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (language == "sw") "EN" else "SW",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { viewModel.downloadFromCloud() }) {
                Icon(Icons.Default.CloudDownload, s.download, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { viewModel.uploadToCloud() }) {
                Icon(Icons.Default.CloudUpload, s.upload, tint = MaterialTheme.colorScheme.primary)
            }
        }

        // ── Sync banner ──────────────────────────────────────────────────────
        when (syncState) {
            SyncState.SYNCING -> SyncBanner(s.syncing, MaterialTheme.colorScheme.secondaryContainer) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.secondary)
            }
            SyncState.SUCCESS -> SyncBanner(s.syncSuccess, MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            SyncState.ERROR -> SyncBanner("${s.syncError}${syncMessage.substringAfter(": ")}", MaterialTheme.colorScheme.errorContainer) {
                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
            SyncState.IDLE -> {}
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Spacer(Modifier.height(0.dp))

            RevenueHeroCard(analytics, s)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickStatChip(s.birdsLabel, totalBirds.toString(), Icons.Default.Fastfood, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                QuickStatChip(s.eggsLabel, todayEggs.toString(), Icons.Default.Egg, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                QuickStatChip(s.tasksLabel, pendingTasks.toString(), Icons.Default.CheckCircle,
                    if (pendingTasks > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                QuickStatChip(s.feedLabel, lowStockCount.toString(), Icons.Default.ShoppingCart,
                    if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(if (selectedDays > 30) s.eggProductionWeekly else s.eggProductionDaily)
                EggTrendCard(analytics, s, selectedDays) { viewModel.setSelectedDays(it) }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(s.birdInventory)
                BirdBreakdownCard(analytics, s)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(s.feedSummary)
                FeedSummaryCard(analytics, s)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel(s.quickActions)
                QuickActionsGrid(s,
                    onAddEggs       = { onNavigateTo("eggs?showAdd=true") },
                    onAddBirds      = { onNavigateTo("birds?showAdd=true") },
                    onAddFeed       = { onNavigateTo("feed?showAdd=true") },
                    onAddTask       = { onNavigateTo("tasks?showAdd=true") },
                    onUploadClick   = { viewModel.uploadToCloud() },
                    onDownloadClick = { viewModel.downloadFromCloud() }
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RevenueHeroCard(analytics: FarmAnalytics, s: com.example.poultryfarmmanager.ui.theme.AppStrings) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(s.totalRevenue, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
            Text("%.0f/-".format(analytics.totalRevenue), style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                RevenueSubItem(s.eggSales, analytics.eggRevenue, Modifier.weight(1f))
                RevenueSubItem(s.birdSales, analytics.birdRevenue, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RevenueSubItem(label: String, amount: Double, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text("%.0f/-".format(amount), style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
    }
}

@Composable
private fun QuickStatChip(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    KukuCard(modifier = modifier, containerColor = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, maxLines = 1)
        }
    }
}


@Composable
private fun EggTrendCard(analytics: FarmAnalytics, s: com.example.poultryfarmmanager.ui.theme.AppStrings, selectedDays: Int, onSelectDays: (Int) -> Unit) {
    val barColor   = MaterialTheme.colorScheme.secondary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    var dropdownExpanded by remember { mutableStateOf(false) }
    KukuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s.eggsCollected, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${s.totalPrefix} ${analytics.totalEggs}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Box {
                        TextButton(
                            onClick = { dropdownExpanded = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${selectedDays}d", style = MaterialTheme.typography.labelMedium)
                            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            RANGE_OPTIONS.forEach { days ->
                                DropdownMenuItem(
                                    text = { Text("${days}d", style = MaterialTheme.typography.bodyMedium) },
                                    onClick = { onSelectDays(days); dropdownExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
            val dataPoints = analytics.eggTrend
            val maxCount   = dataPoints.maxOfOrNull { it.count }?.takeIf { it > 0 } ?: 1
            Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
                val n = dataPoints.size; if (n == 0) return@Canvas
                val gap = size.width * 0.015f; val barWidth = (size.width - gap * (n - 1)) / n
                val chartH = size.height - 20.dp.toPx()
                dataPoints.forEachIndexed { i, point ->
                    val barH = (point.count.toFloat() / maxCount) * chartH; val x = i * (barWidth + gap)
                    drawRect(color = barColor.copy(alpha = 0.3f + 0.7f * (point.count.toFloat() / maxCount)),
                        topLeft = Offset(x, chartH - barH), size = Size(barWidth, barH))
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                analytics.eggTrend.forEach { point ->
                    Text(point.label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall, color = labelColor, fontSize = 9.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun BirdBreakdownCard(analytics: FarmAnalytics, s: com.example.poultryfarmmanager.ui.theme.AppStrings) {
    KukuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BreedStatChip(s.birdSold, analytics.totalBirdsSold, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                BreedStatChip(s.birdSlaughtered, analytics.totalBirdsSlaughtered, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
            if (analytics.birdBreakdown.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                analytics.birdBreakdown.forEach { Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(it.breed, style = MaterialTheme.typography.bodyMedium)
                    Text("${it.remaining} ${s.birdsSuffix}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }}
            }
        }
    }
}

@Composable
private fun BreedStatChip(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.1f)).padding(12.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeedSummaryCard(analytics: FarmAnalytics, s: com.example.poultryfarmmanager.ui.theme.AppStrings) {
    KukuCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedStatItem(s.totalCost,  "%.0f/-".format(analytics.totalFeedCost), Modifier.weight(1f))
            FeedStatItem(s.feedTypes,  analytics.feedTypeCount.toString(),        Modifier.weight(1f))
            FeedStatItem(s.lowStock,   analytics.lowStockCount.toString(),        Modifier.weight(1f),
                if (analytics.lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun FeedStatItem(label: String, value: String, modifier: Modifier, valueColor: Color = MaterialTheme.colorScheme.primary) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = valueColor)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SyncBanner(message: String, color: Color, icon: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
        .background(color).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        icon(); Text(message, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun QuickActionsGrid(s: com.example.poultryfarmmanager.ui.theme.AppStrings, onAddEggs: () -> Unit, onAddBirds: () -> Unit, onAddFeed: () -> Unit, onAddTask: () -> Unit, onUploadClick: () -> Unit, onDownloadClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPill(Icons.Default.Egg, s.logEggs, Modifier.weight(1f), onAddEggs)
            ActionPill(Icons.Default.Add, s.addBirds, Modifier.weight(1f), onAddBirds)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPill(Icons.Default.ShoppingCart, s.addFeed, Modifier.weight(1f), onAddFeed)
            ActionPill(Icons.Default.CheckCircle, s.addTask, Modifier.weight(1f), onAddTask)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionPill(Icons.Default.CloudUpload, s.upload, Modifier.weight(1f), onUploadClick, MaterialTheme.colorScheme.primary)
            ActionPill(Icons.Default.CloudDownload, s.download, Modifier.weight(1f), onDownloadClick, MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}, accentColor: Color = MaterialTheme.colorScheme.onSurface) {
    Card(onClick = onClick, modifier = modifier.height(64.dp), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, label, tint = accentColor, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
