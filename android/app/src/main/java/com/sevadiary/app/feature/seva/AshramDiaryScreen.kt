package com.sevadiary.app.feature.seva

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.DevotionalCard
import com.sevadiary.app.core.designsystem.component.DevotionalEmptyState
import com.sevadiary.app.core.designsystem.component.DevotionalHeader
import com.sevadiary.app.core.designsystem.component.SevaTextField
import com.sevadiary.app.core.designsystem.theme.ForestGreen
import com.sevadiary.app.core.designsystem.theme.SaffronGold
import com.sevadiary.app.core.network.dto.SevaRecordDto
import com.sevadiary.app.feature.seva.presentation.SevaViewModel

@Composable
fun AshramDiaryScreen(
    viewModel: SevaViewModel,
    onBackClick: () -> Unit,
    onAddNewVisit: () -> Unit,
    onEditVisit: (visitId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var recordToDelete by remember { mutableStateOf<SevaRecordDto?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DevotionalHeader(
                title = stringResource(R.string.ashram_diary_title),
                subtitle = "Document Visits, Seva & Memories",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { viewModel.loadRecords() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewVisit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Visit")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                SevaTextField(
                    value = searchInput,
                    onValueChange = {
                        searchInput = it
                        viewModel.onSearchQueryChange(it)
                    },
                    label = "Search by ashram, notes or location",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (searchInput.isNotBlank()) {
                        {
                            IconButton(onClick = {
                                searchInput = ""
                                viewModel.onSearchQueryChange("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    } else null
                )
            }

            // Ashram Filter Chips Row
            val ashrams = uiState.masterData?.ashrams ?: emptyList()
            if (ashrams.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filterAshram == null,
                            onClick = { viewModel.onFilterAshram(null) },
                            label = { Text("All Ashrams") }
                        )
                    }
                    items(ashrams) { ashram ->
                        FilterChip(
                            selected = uiState.filterAshram == ashram.code,
                            onClick = {
                                val nextFilter = if (uiState.filterAshram == ashram.code) null else ashram.code
                                viewModel.onFilterAshram(nextFilter)
                            },
                            label = { Text(ashram.labels.en.replace("Satlok Ashram ", "")) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visit Records List or Empty State
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.records.isEmpty()) {
                DevotionalEmptyState(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    title = stringResource(R.string.no_visits_found),
                    message = "Tap the + button to record your visits to Satlok Ashrams, inclusive dates, seva duties, and memorable spiritual notes.",
                    actionText = stringResource(R.string.ashram_new_visit),
                    onActionClick = onAddNewVisit
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.records, key = { it.id }) { record ->
                        val ashramName = if (record.ashramCode == "OTHER_ASHRAM" && record.customAshramName.isNotBlank()) {
                            record.customAshramName
                        } else {
                            val found = ashrams.find { it.code == record.ashramCode }
                            found?.labels?.en ?: record.ashramCode
                        }

                        DevotionalCard(
                            onClick = { onEditVisit(record.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ashramName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${record.startDate} → ${record.endDate} (${record.durationDays} days)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditVisit(record.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { recordToDelete = record },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Badges for Purpose & Seva Type
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = record.purpose,
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    )
                                }

                                if (record.sevaType.isNotBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SaffronGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = record.sevaType.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall.copy(color = SaffronGold, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            if (record.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = record.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Ashram Visit") },
            text = { Text("Are you sure you want to delete this visit record? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    recordToDelete?.id?.let { viewModel.deleteRecord(it) }
                    recordToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
