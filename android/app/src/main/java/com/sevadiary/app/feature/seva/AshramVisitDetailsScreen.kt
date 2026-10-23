package com.sevadiary.app.feature.seva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.text.SimpleDateFormat
import java.util.Locale

// Devotional Theme Palette
private val AshramBgColor = Color(0xFFFAF8F2)
private val AshramForestGreen = Color(0xFF174C38)
private val AshramDarkGreen = Color(0xFF103C2D)
private val AshramSaffronGold = Color(0xFFD99A32)
private val AshramPrimaryText = Color(0xFF252B27)
private val AshramSecondaryText = Color(0xFF646B65)
private val AshramCardBg = Color(0xFFFFFFFF)
private val AshramBorderColor = Color(0xFFE5DFD3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AshramVisitDetailsScreen(
    viewModel: SevaViewModel,
    visitId: String,
    onBackClick: () -> Unit,
    onEditVisit: (visitId: String) -> Unit,
    onDeleteVisitSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Ensure records are loaded if opening directly or after app reload
    LaunchedEffect(visitId) {
        if (uiState.records.isEmpty() && !uiState.isLoading) {
            viewModel.loadRecords()
        }
    }

    val record = uiState.records.find { it.id == visitId }

    Scaffold(
        containerColor = AshramBgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.visit_details_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AshramDarkGreen,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.visit_details_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AshramSecondaryText,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AshramPrimaryText
                        )
                    }
                },
                actions = {
                    if (record != null) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = AshramSecondaryText
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(AshramCardBg)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.btn_edit_visit), fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = AshramForestGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onEditVisit(record.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            stringResource(R.string.btn_delete_visit),
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 14.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AshramBgColor)
            )
        }
    ) { innerPadding ->
        if (record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = AshramForestGreen)
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AshramSecondaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.visit_not_found),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AshramPrimaryText
                            ),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AshramForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Back", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Render Populated Visit Details
            VisitDetailsContent(
                record = record,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onEditClick = { onEditVisit(record.id) },
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && record != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(record.id)
                        showDeleteDialog = false
                        onDeleteVisitSuccess()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.btn_cancel), color = AshramPrimaryText)
                }
            },
            containerColor = AshramCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun VisitDetailsContent(
    record: SevaRecordDto,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scrollState = rememberScrollState()

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

    // Resolved Purpose Label (displays actual user-entered content if "Other" was selected)
    val purposeBadgeText = when {
        isOtherPurpose && record.customPurposeOrSeva.isNotBlank() -> record.customPurposeOrSeva
        isSeva && isOtherSeva && record.customPurposeOrSeva.isNotBlank() -> record.customPurposeOrSeva
        isSeva && record.sevaType.isNotBlank() -> stringResource(AshramMasterData.getSevaTypeNameRes(record.sevaType))
        else -> stringResource(AshramMasterData.getPurposeNameRes(record.purpose))
    }

    // Devotional reflection text
    val reflectionText = when {
        record.notes.isNotBlank() && record.notes.length <= 120 -> record.notes
        else -> stringResource(R.string.visit_reflection_default)
    }

    // Seva activities list
    val sevaActivitiesList = remember(record) {
        val list = mutableListOf<String>()
        if (record.sevaType.isNotBlank()) {
            val resolvedSeva = if (isOtherSeva && record.customPurposeOrSeva.isNotBlank()) {
                record.customPurposeOrSeva
            } else {
                record.sevaType.replace("_", " ").lowercase()
                    .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            }
            list.add(resolvedSeva)
        }
        if (isOtherPurpose && record.customPurposeOrSeva.isNotBlank() && !list.contains(record.customPurposeOrSeva)) {
            list.add(record.customPurposeOrSeva)
        }
        if (list.isEmpty()) {
            list.add("Bhandara seva")
            list.add("Langar seva")
            list.add("General seva at ashram premises")
        } else {
            list.add("General seva at ashram premises")
        }
        list
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // 1. Prominent Satlok Ashram Photograph Hero (onboarding3.png)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .height(230.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding3),
                contentDescription = "Satlok Ashram Entrance",
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle badge on the bottom-right of the hero image
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = "1/8",
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Primary Visit Title & Header Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = ashramName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = AshramDarkGreen,
                    fontSize = 21.sp
                )
            )

            Spacer(modifier = Modifier.height(5.dp))

            // Location Row
            if (displayLocation.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = AshramForestGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = displayLocation,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AshramSecondaryText,
                            fontSize = 13.5.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
            }

            // Dates & Duration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AshramSecondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayDateRange,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AshramSecondaryText,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD9ECE1)
                ) {
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AshramDarkGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Devotional Quote / Reflection Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFEEF5F1),
            border = BorderStroke(1.dp, Color(0xFFDCE8E0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "❝",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = AshramForestGreen,
                        fontSize = 22.sp
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = reflectionText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF2C4638),
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Visit Information Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.visit_info_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AshramDarkGreen,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Purpose
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = AshramForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.visit_info_purpose),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AshramPrimaryText,
                            fontSize = 14.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFDF1DF)
                ) {
                    Text(
                        text = purposeBadgeText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFA66408),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = AshramBorderColor.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Visit type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = AshramForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.visit_info_type),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AshramPrimaryText,
                            fontSize = 14.sp
                        )
                    )
                }

                Text(
                    text = stringResource(R.string.visit_info_type_personal),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = AshramPrimaryText,
                        fontSize = 13.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = AshramBorderColor.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF1E7E34),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.visit_info_status),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AshramPrimaryText,
                            fontSize = 14.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE2F2E7)
                ) {
                    Text(
                        text = stringResource(R.string.visit_status_completed),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF1E7E34),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 5. Seva Activities Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_devotional_namaskar),
                    contentDescription = null,
                    tint = AshramSaffronGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.visit_seva_activities),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AshramDarkGreen,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            sevaActivitiesList.forEach { activity ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(AshramSecondaryText)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = activity,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF4A544E),
                            fontSize = 13.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 6. Memories & Notes Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = AshramSaffronGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.visit_memories_notes),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AshramDarkGreen,
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val notesContent = if (record.notes.isNotBlank()) {
                record.notes
            } else {
                "It was a very peaceful and memorable experience. Darshan of Sadgurudev Ji gave immense peace. Participated in bhandara seva and had the opportunity to serve many devotees."
            }

            Text(
                text = notesContent,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF4A544E),
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // 7. Bottom Action Buttons: Edit Visit & Delete Visit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Edit Visit Button
            Surface(
                onClick = onEditClick,
                shape = RoundedCornerShape(16.dp),
                color = AshramForestGreen,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_edit_visit),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            // Delete Visit Button
            Surface(
                onClick = onDeleteClick,
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF6F6),
                border = BorderStroke(1.dp, Color(0xFFF3C7C7)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.btn_delete_visit),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    )
                }
            }
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
