package com.sevadiary.app.feature.seva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Seva Diary Devotional Palette
private val AshramBgColor = Color(0xFFFAF8F2)
private val AshramForestGreen = Color(0xFF174C38)
private val AshramDarkGreen = Color(0xFF103C2D)
private val AshramSaffronGold = Color(0xFFD99A32)
private val AshramLightGold = Color(0xFFF4D58A)
private val AshramPrimaryText = Color(0xFF252B27)
private val AshramSecondaryText = Color(0xFF646B65)
private val AshramCardBg = Color(0xFFFFFFFF)
private val AshramBorderColor = Color(0xFFE5DFD3)

@Composable
fun AshramDiaryScreen(
    viewModel: SevaViewModel,
    onBackClick: () -> Unit,
    onAddNewVisit: () -> Unit,
    onViewVisitDetails: (visitId: String) -> Unit,
    onEditVisit: (visitId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var recordToDelete by remember { mutableStateOf<SevaRecordDto?>(null) }
    var sortDescending by remember { mutableStateOf(true) } // true: Latest First, false: Oldest First
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Ensure fresh records on screen entry if not already loading
    LaunchedEffect(Unit) {
        if (uiState.records.isEmpty() && !uiState.isLoading) {
            viewModel.loadRecords()
        }
    }

    val filteredRecords = remember(uiState.records, searchInput, uiState.filterAshram, sortDescending) {
        val list = uiState.records.filter { record ->
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
        if (sortDescending) {
            list.sortedByDescending { it.startDate }
        } else {
            list.sortedBy { it.startDate }
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
                    shape = RoundedCornerShape(24.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Log Visit", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Devotional Hero Banner (Visible when records exist or are loading)
            if (uiState.records.isNotEmpty() || uiState.isLoading) {
                DevotionalHeroBanner()
            }

            // 1. Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 5.dp)
            ) {
                AshramSearchBar(
                    query = searchInput,
                    onQueryChange = {
                        searchInput = it
                        viewModel.onSearchQueryChange(it)
                    }
                )
            }

            // 2. Horizontally Scrollable Location Filter Chips
            if (uiState.records.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 20.dp, end = 8.dp),
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
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                ) {
                                    if (code != null) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_devotional_ashram),
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else AshramSaffronGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else AshramPrimaryText,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Trailing Right Chevron Indicator Chip
                    Surface(
                        shape = CircleShape,
                        color = AshramCardBg,
                        border = BorderStroke(1.dp, AshramBorderColor),
                        modifier = Modifier
                            .padding(end = 20.dp)
                            .size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "More ashrams",
                                tint = AshramSecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 3. Section Header: "Your Visits" with visit count badge and Sort Action
            if (uiState.records.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.ashram_your_visits),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AshramDarkGreen,
                                fontSize = 17.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE4EDE7)
                        ) {
                            val countText = if (filteredRecords.size == 1) {
                                stringResource(R.string.ashram_visits_single)
                            } else {
                                stringResource(R.string.ashram_visits_count, filteredRecords.size)
                            }
                            Text(
                                text = countText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = AshramForestGreen,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Sort Selector
                    Box {
                        Surface(
                            onClick = { sortMenuExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    tint = AshramSecondaryText,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (sortDescending) {
                                        stringResource(R.string.ashram_sort_latest)
                                    } else {
                                        stringResource(R.string.ashram_sort_oldest)
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AshramPrimaryText
                                    )
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = AshramSecondaryText,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier.background(AshramCardBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ashram_sort_latest), fontSize = 13.sp) },
                                onClick = {
                                    sortDescending = true
                                    sortMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ashram_sort_oldest), fontSize = 13.sp) },
                                onClick = {
                                    sortDescending = false
                                    sortMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 4. Main Content: Loading, Empty State, or Populated Visit List
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
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredRecords, key = { it.id }) { record ->
                                AshramVisitCard(
                                    record = record,
                                    onViewDetails = { onViewVisitDetails(record.id) },
                                    onEdit = { onEditVisit(record.id) },
                                    onDelete = { recordToDelete = record }
                                )
                            }

                            // Subtle devotional footer flourish at bottom of scrollable list
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                DevotionalGoldSeparator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 40.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
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
                    text = stringResource(R.string.ashram_delete_title),
                    fontWeight = FontWeight.Bold,
                    color = AshramPrimaryText
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.ashram_delete_confirm),
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
 * Top Devotional Hero Banner with Quotation, Lotus Motif, and Satlok Ashram Entrance photograph.
 */
@Composable
private fun DevotionalHeroBanner() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF7F3E9),
        border = BorderStroke(1.dp, Color(0xFFEADFCB)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .height(124.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Right side: Satlok Ashram Entrance photograph (onboarding3.png)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding3),
                    contentDescription = "Satlok Ashram Entrance",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.56f)
                        .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                )
            }

            // Smooth Horizontal Gradient Overlay to blend artwork into warm ivory background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFF7F3E9),
                                Color(0xFFF7F3E9),
                                Color(0xFFF7F3E9).copy(alpha = 0.85f),
                                Color(0xFFF7F3E9).copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 600f
                        )
                    )
            )

            // Left side: Devotional Quotation and Lotus divider
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.58f)
                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.ashram_quote_text),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        lineHeight = 19.sp,
                        color = AshramDarkGreen
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(1.dp)
                            .background(AshramSaffronGold.copy(alpha = 0.7f))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_devotional_lotus),
                        contentDescription = null,
                        tint = AshramSaffronGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(AshramSaffronGold)
                    )
                }
            }
        }
    }
}

/**
 * Compact, elegant visit card matching the exact devotional UI design.
 */
@Composable
private fun AshramVisitCard(
    record: SevaRecordDto,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val isOtherAshram = AshramMasterData.isOtherAshram(record.ashramCode)
    val ashramName = when {
        isOtherAshram && record.customAshramName.isNotBlank() -> record.customAshramName
        isOtherAshram -> stringResource(R.string.ashram_other)
        else -> {
            val ashramLoc = AshramMasterData.findByCode(record.ashramCode)
            ashramLoc?.let { stringResource(it.nameRes) }
                ?: record.ashramCode.replace("_", " ")
        }
    }

    val displayLocation = resolveAshramLocation(record)
    val formattedStart = formatVisitDisplayDate(record.startDate)
    val formattedEnd = formatVisitDisplayDate(record.endDate)
    val displayDateRange = if (record.startDate == record.endDate || formattedEnd.isBlank()) {
        formattedStart
    } else {
        "$formattedStart – $formattedEnd"
    }
    val durationText = if (record.durationDays <= 1) "1 day" else "${record.durationDays} days"

    val isOtherPurpose = AshramMasterData.isOtherPurpose(record.purpose)
    val isSeva = record.purpose.equals("SEVA", ignoreCase = true)
    val isOtherSeva = AshramMasterData.isOtherSeva(record.sevaType)

    // Purpose Badge Text: If "Other" was chosen, show actual entered content!
    val purposeBadgeText = when {
        isOtherPurpose && record.customPurposeOrSeva.isNotBlank() -> record.customPurposeOrSeva
        isSeva && isOtherSeva && record.customPurposeOrSeva.isNotBlank() -> record.customPurposeOrSeva
        isSeva && record.sevaType.isNotBlank() -> stringResource(AshramMasterData.getSevaTypeNameRes(record.sevaType))
        else -> stringResource(AshramMasterData.getPurposeNameRes(record.purpose))
    }

    // Purpose Chip Styling & Icon
    val (chipBgColor, chipTextColor, chipIcon) = getPurposeChipStyle(
        purpose = record.purpose,
        isOtherPurpose = isOtherPurpose
    )

    Surface(
        onClick = onViewDetails,
        shape = RoundedCornerShape(16.dp),
        color = AshramCardBg,
        border = BorderStroke(1.dp, Color(0xFFEBE6DD)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 1. Left Thumbnail: Ashram Photograph (onboarding3.png)
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 90.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding3),
                    contentDescription = "Ashram View",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Right Details Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                // Ashram Title & 3-dots Menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = ashramName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AshramDarkGreen,
                            fontSize = 14.5.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(24.dp)
                                .offset(x = 4.dp, y = (-2).dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = AshramSecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(AshramCardBg)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ashram_view_details), fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = AshramForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onViewDetails()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.btn_edit_visit), fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = AshramForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.btn_delete_visit),
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Location row
                if (displayLocation.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = AshramForestGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayLocation,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = AshramSecondaryText
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                }

                // Date & Duration row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AshramSecondaryText,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$displayDateRange ($durationText)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = AshramSecondaryText,
                            fontWeight = FontWeight.Normal
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: Purpose chip on left & View Details on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Purpose Badge Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = chipBgColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                        ) {
                            Icon(
                                imageVector = chipIcon,
                                contentDescription = null,
                                tint = chipTextColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = purposeBadgeText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = chipTextColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // View Details Action Button
                    Surface(
                        onClick = onViewDetails,
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F6F3)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ashram_view_details),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = AshramForestGreen
                                )
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = AshramForestGreen,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Returns customized soft background color, text color, and icon for devotional purpose categories.
 */
private fun getPurposeChipStyle(
    purpose: String,
    isOtherPurpose: Boolean
): Triple<Color, Color, ImageVector> {
    return when {
        purpose.equals("BHANDARA", ignoreCase = true) -> {
            Triple(
                Color(0xFFFDF2E2), // soft peach/amber
                Color(0xFFA66408), // dark amber
                Icons.Default.SoupKitchen
            )
        }
        purpose.equals("SEVA", ignoreCase = true) -> {
            Triple(
                Color(0xFFEAF4EE), // soft sage green
                AshramForestGreen,
                Icons.Default.VolunteerActivism
            )
        }
        purpose.equals("SATSANG", ignoreCase = true) -> {
            Triple(
                Color(0xFFEBF2ED), // gentle green
                Color(0xFF1B4F3A),
                Icons.Default.SelfImprovement
            )
        }
        purpose.equals("NAAM_DAAN", ignoreCase = true) -> {
            Triple(
                Color(0xFFFFF7E6),
                Color(0xFFB87000),
                Icons.Default.AutoAwesome
            )
        }
        isOtherPurpose -> {
            Triple(
                Color(0xFFE8F1EC), // soft muted green
                AshramForestGreen,
                Icons.Default.Groups
            )
        }
        else -> {
            Triple(
                Color(0xFFE8F1EC),
                AshramForestGreen,
                Icons.Default.Spa
            )
        }
    }
}

/**
 * Resolves formatted location label based on master data or custom details.
 */
private fun resolveAshramLocation(record: SevaRecordDto): String {
    if (record.locationDetails.isNotBlank()) return record.locationDetails
    val master = AshramMasterData.findByCode(record.ashramCode)
    return when {
        master != null && master.code == "DHANANA_DHAM" -> "Dhanana Dham"
        master != null && master.code == "KARONTHA_DHAM" -> "Karontha Dham"
        master != null && master.stateOrCountry.isNotBlank() -> {
            val city = master.code.replace("_", " ").lowercase()
                .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            "$city, ${master.stateOrCountry}"
        }
        master != null -> master.code.replace("_", " ")
        record.customAshramName.isNotBlank() -> record.customAshramName
        else -> "Satlok Ashram"
    }
}

/**
 * Parses and formats dates from 'yyyy-MM-dd' to 'dd MMM yyyy' safely.
 */
private fun formatVisitDisplayDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        if (date != null) outputFormat.format(date) else dateStr
    } catch (e: Exception) {
        dateStr
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
                    color = AshramDarkGreen
                )
            )
            Text(
                text = stringResource(R.string.ashram_header_subtitle),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = AshramSecondaryText,
                    fontSize = 12.5.sp
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
                    fontSize = 13.5.sp
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
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = AshramSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(26.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
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
            modifier = Modifier.size(22.dp)
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
