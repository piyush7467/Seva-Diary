package com.sevadiary.app.feature.seva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.network.dto.SevaRecordDto
import com.sevadiary.app.feature.seva.data.AshramMasterData
import com.sevadiary.app.feature.seva.presentation.SevaViewModel

// Seva Diary Devotional Palette
private val AshramBgColor = Color(0xFFFAF8F2)
private val AshramForestGreen = Color(0xFF174C38)
private val AshramDarkGreen = Color(0xFF103C2D)
private val AshramSaffronGold = Color(0xFFD99A32)
private val AshramLightGold = Color(0xFFF4D58A)
private val AshramPrimaryText = Color(0xFF252B27)
private val AshramSecondaryText = Color(0xFF646B65)
private val AshramCardBg = Color(0xFFFFFFFF)
private val AshramBorderColor = Color(0xFFE2DDD3)

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

    // Ensure fresh records on screen entry if not already loading
    LaunchedEffect(Unit) {
        if (uiState.records.isEmpty() && !uiState.isLoading) {
            viewModel.loadRecords()
        }
    }

    val filteredRecords = remember(uiState.records, searchInput, uiState.filterAshram) {
        uiState.records.filter { record ->
            val matchesFilter = uiState.filterAshram == null || record.ashramCode.equals(uiState.filterAshram, ignoreCase = true)
            val query = searchInput.trim().lowercase()
            val matchesSearch = query.isBlank() ||
                record.customAshramName.lowercase().contains(query) ||
                record.customPurposeOrSeva.lowercase().contains(query) ||
                record.notes.lowercase().contains(query) ||
                record.locationDetails.lowercase().contains(query) ||
                record.purpose.lowercase().contains(query) ||
                record.sevaType.lowercase().contains(query)
            matchesFilter && matchesSearch
        }
    }

    val allAshramsLabel = stringResource(R.string.ashram_filter_all)
    val filterItems = remember(allAshramsLabel) {
        val list = mutableListOf<Pair<String?, Int>>()
        list.add(null to R.string.ashram_filter_all)
        AshramMasterData.ashrams.forEach { loc ->
            list.add(loc.code to loc.nameRes)
        }
        list
    }

    Scaffold(
        containerColor = AshramBgColor,
        topBar = {
            AshramCompactHeader(
                onBackClick = onBackClick,
                onRefreshClick = { viewModel.loadRecords() }
            )
        },
        floatingActionButton = {
            if (uiState.records.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAddNewVisit,
                    containerColor = AshramForestGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Log Visit", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                AshramSearchBar(
                    query = searchInput,
                    onQueryChange = {
                        searchInput = it
                        viewModel.onSearchQueryChange(it)
                    }
                )
            }

            // 2. Location Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterItems) { (code, nameRes) ->
                    val rawName = stringResource(nameRes)
                    val label = if (code == null) {
                        rawName
                    } else {
                        rawName
                            .replace("Satlok Ashram ", "")
                            .replace("Shri ", "")
                            .replace("Other Ashram / Location", "Other")
                            .replace("सतलोक आश्रम ", "")
                            .replace("श्री ", "")
                            .replace("अन्य आश्रम / स्थान", "अन्य")
                            .trim()
                    }
                    val isSelected = uiState.filterAshram == code
                    Surface(
                        onClick = {
                            val next = if (uiState.filterAshram == code) null else code
                            viewModel.onFilterAshram(next)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) AshramForestGreen else AshramCardBg,
                        border = if (isSelected) null else BorderStroke(1.dp, AshramBorderColor),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else AshramPrimaryText,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. Main Content: Loading, Empty State, or Populated Visit List
            when {
                uiState.isLoading && uiState.records.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AshramForestGreen,
                            strokeWidth = 3.dp
                        )
                    }
                }

                uiState.records.isEmpty() -> {
                    // Devotional Empty State matching design
                    AshramEmptyState(
                        errorMessage = uiState.errorMessage,
                        onAddNewVisit = onAddNewVisit,
                        onRefreshClick = { viewModel.loadRecords() }
                    )
                }

                filteredRecords.isEmpty() -> {
                    // Search or filter returned 0 results from existing records
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = AshramSecondaryText,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No visits match your search or filter.",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AshramPrimaryText
                                ),
                                textAlign = TextAlign.Center
                            )
                            TextButton(
                                onClick = {
                                    searchInput = ""
                                    viewModel.onSearchQueryChange("")
                                    viewModel.onFilterAshram(null)
                                }
                            ) {
                                Text("Clear Filters", color = AshramForestGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                else -> {
                    // Populated Visit List
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = AshramForestGreen,
                                trackColor = AshramBorderColor
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 88.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredRecords, key = { it.id }) { record ->
                                val isOtherAshram = AshramMasterData.isOtherAshram(record.ashramCode)
                                val ashramName = when {
                                    isOtherAshram && record.customAshramName.isNotBlank() -> record.customAshramName
                                    isOtherAshram -> stringResource(R.string.ashram_other)
                                    else -> {
                                        val ashramLoc = AshramMasterData.findByCode(record.ashramCode)
                                        ashramLoc?.let { stringResource(it.nameRes) }
                                            ?: uiState.masterData?.ashrams?.find { it.code.equals(record.ashramCode, ignoreCase = true) }?.labels?.en
                                            ?: record.ashramCode.replace("_", " ")
                                    }
                                }

                                val isOtherPurpose = AshramMasterData.isOtherPurpose(record.purpose)
                                val isSeva = record.purpose.equals("SEVA", ignoreCase = true)
                                val isOtherSeva = AshramMasterData.isOtherSeva(record.sevaType)

                                // Purpose Badge: If user chose "Other", display their actual entered content!
                                val purposeBadgeText = when {
                                    isOtherPurpose && record.customPurposeOrSeva.isNotBlank() -> record.customPurposeOrSeva
                                    else -> stringResource(AshramMasterData.getPurposeNameRes(record.purpose))
                                }

                                // Seva Badge: If purpose is SEVA, display seva type (or custom content if Other Seva was chosen)
                                val sevaBadgeText = if (isSeva && record.sevaType.isNotBlank()) {
                                    when {
                                        isOtherSeva && record.customPurposeOrSeva.isNotBlank() -> record.customPurposeOrSeva
                                        else -> stringResource(AshramMasterData.getSevaTypeNameRes(record.sevaType))
                                    }
                                } else null

                                Surface(
                                    onClick = { onEditVisit(record.id) },
                                    shape = RoundedCornerShape(16.dp),
                                    color = AshramCardBg,
                                    border = BorderStroke(1.dp, AshramBorderColor),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
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
                                                        color = AshramForestGreen,
                                                        fontSize = 16.sp
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CalendarToday,
                                                        contentDescription = null,
                                                        tint = AshramSecondaryText,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${record.startDate} → ${record.endDate} (${record.durationDays} days)",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Medium,
                                                            color = AshramSecondaryText,
                                                            fontSize = 13.sp
                                                        )
                                                    )
                                                }
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = { onEditVisit(record.id) },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = AshramForestGreen,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { recordToDelete = record },
                                                    modifier = Modifier.size(36.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Badges for Purpose & Seva Type
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AshramForestGreen.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = purposeBadgeText,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = AshramForestGreen,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            if (!sevaBadgeText.isNullOrBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = AshramSaffronGold.copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = sevaBadgeText,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = AshramSaffronGold,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // If user entered custom purpose/seva details while selecting a standard category, display it here
                                        if (!isOtherPurpose && !(isSeva && isOtherSeva) && record.customPurposeOrSeva.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = record.customPurposeOrSeva,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = AshramPrimaryText,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp
                                                ),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                    if (record.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = record.notes,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = AshramSecondaryText,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (record.locationDetails.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = AshramSecondaryText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = record.locationDetails,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = AshramSecondaryText,
                                                    fontSize = 12.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
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
            title = {
                Text(
                    text = "Delete Ashram Visit",
                    fontWeight = FontWeight.Bold,
                    color = AshramPrimaryText
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this visit record? This action cannot be undone.",
                    color = AshramSecondaryText
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    recordToDelete?.id?.let { viewModel.deleteRecord(it) }
                    recordToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(stringResource(R.string.btn_cancel), color = AshramPrimaryText)
                }
            },
            containerColor = AshramCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Compact Header matching design reference.
 */
@Composable
private fun AshramCompactHeader(
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = AshramPrimaryText,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ashram_diary_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AshramForestGreen
                )
            )
            Text(
                text = stringResource(R.string.ashram_header_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AshramSecondaryText,
                    fontSize = 13.sp
                )
            )
        }

        IconButton(onClick = onRefreshClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = AshramForestGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Clean search field with rounded corners.
 */
@Composable
private fun AshramSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.ashram_diary_search_placeholder),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AshramSecondaryText.copy(alpha = 0.8f),
                    fontSize = 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = AshramSecondaryText,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = AshramSecondaryText
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AshramCardBg,
            unfocusedContainerColor = AshramCardBg,
            focusedBorderColor = AshramForestGreen,
            unfocusedBorderColor = AshramBorderColor,
            cursorColor = AshramForestGreen
        )
    )
}

/**
 * Devotional Empty State matching the UI reference.
 */
@Composable
private fun AshramEmptyState(
    errorMessage: String? = null,
    onAddNewVisit: () -> Unit,
    onRefreshClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // If there is an actual network or connection error, show a polite alert banner with retry
        val displayError = if (errorMessage?.contains("Authentication required", ignoreCase = true) == true) {
            null
        } else {
            errorMessage
        }

        if (!displayError.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFDF2E9),
                border = BorderStroke(1.dp, Color(0xFFF3C89B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = AshramSaffronGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = displayError,
                        style = MaterialTheme.typography.bodySmall.copy(color = AshramPrimaryText),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRefreshClick) {
                        Text("Retry", fontWeight = FontWeight.Bold, color = AshramForestGreen)
                    }
                }
            }
        }

        // 1. Wide landscape card of the Satlok Ashram Entrance Photograph (onboarding3.png)
        Surface(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFEADBBE)),
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding3),
                contentDescription = "Satlok Ashram Entrance",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Gold Decorative Separator with lotus and ornamental lines
        DevotionalGoldSeparator()

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Prominent Empty-state Title
        Text(
            text = stringResource(R.string.ashram_empty_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = AshramForestGreen,
                fontSize = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Description
        Text(
            text = stringResource(R.string.ashram_empty_desc),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AshramSecondaryText,
                fontSize = 14.sp,
                lineHeight = 21.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Primary Action Button: "+ Log New Ashram Visit →"
        Surface(
            onClick = onAddNewVisit,
            shape = RoundedCornerShape(16.dp),
            color = AshramForestGreen,
            shadowElevation = 3.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ashram_new_visit),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Three Feature Highlights Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeatureHighlightItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AshramSaffronGold,
                        modifier = Modifier.size(20.dp)
                    )
                },
                title = stringResource(R.string.ashram_feat_dates_title),
                desc = stringResource(R.string.ashram_feat_dates_desc),
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(AshramBorderColor.copy(alpha = 0.7f))
            )

            FeatureHighlightItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_devotional_namaskar),
                        contentDescription = null,
                        tint = AshramSaffronGold,
                        modifier = Modifier.size(20.dp)
                    )
                },
                title = stringResource(R.string.ashram_feat_seva_title),
                desc = stringResource(R.string.ashram_feat_seva_desc),
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(AshramBorderColor.copy(alpha = 0.7f))
            )

            FeatureHighlightItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = AshramSaffronGold,
                        modifier = Modifier.size(20.dp)
                    )
                },
                title = stringResource(R.string.ashram_feat_notes_title),
                desc = stringResource(R.string.ashram_feat_notes_desc),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Delicate golden ornamental separator with lotus and flourishes.
 */
@Composable
private fun DevotionalGoldSeparator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left gradient line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, AshramSaffronGold.copy(alpha = 0.7f))
                    )
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .rotate(45f)
                .background(AshramSaffronGold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(AshramSaffronGold)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Center Sacred Lotus Motif
        Icon(
            painter = painterResource(id = R.drawable.ic_devotional_lotus),
            contentDescription = null,
            tint = AshramSaffronGold,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(AshramSaffronGold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(5.dp)
                .rotate(45f)
                .background(AshramSaffronGold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        // Right gradient line
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(AshramSaffronGold.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )
    }
}

/**
 * Individual item for the 3-feature row in the empty state.
 */
@Composable
private fun FeatureHighlightItem(
    icon: @Composable () -> Unit,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF9F1DF),
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AshramPrimaryText
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.5.sp,
                color = AshramSecondaryText,
                lineHeight = 14.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}
