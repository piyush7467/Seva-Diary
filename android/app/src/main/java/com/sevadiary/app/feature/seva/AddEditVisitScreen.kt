package com.sevadiary.app.feature.seva

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.*
import com.sevadiary.app.core.designsystem.theme.ForestGreen
import com.sevadiary.app.core.designsystem.theme.SaffronGold
import com.sevadiary.app.feature.seva.presentation.SevaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVisitScreen(
    viewModel: SevaViewModel,
    visitId: String? = null,
    onBackClick: () -> Unit,
    onSavedSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val existingRecord = uiState.records.find { it.id == visitId }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedAshramCode by remember { mutableStateOf(existingRecord?.ashramCode ?: "BARWALA") }
    var customAshramName by remember { mutableStateOf(existingRecord?.customAshramName ?: "") }
    var startDate by remember { mutableStateOf(existingRecord?.startDate ?: todayStr) }
    var endDate by remember { mutableStateOf(existingRecord?.endDate ?: todayStr) }
    var selectedPurpose by remember { mutableStateOf(existingRecord?.purpose ?: "SATSANG") }
    var selectedSevaType by remember { mutableStateOf(existingRecord?.sevaType ?: "BHANDARA_SEVA") }
    var customPurposeOrSeva by remember { mutableStateOf(existingRecord?.customPurposeOrSeva ?: "") }
    var notes by remember { mutableStateOf(existingRecord?.notes ?: "") }
    var locationDetails by remember { mutableStateOf(existingRecord?.locationDetails ?: "") }

    var localError by remember { mutableStateOf<String?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val initialMillis = remember(startDate) { parseDateToUtcMillis(startDate) }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val chosenDate = utcMillisToDateString(millis)
                            startDate = chosenDate
                            if (endDate < chosenDate) {
                                endDate = chosenDate
                            }
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.btn_ok), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val initialMillis = remember(endDate) { parseDateToUtcMillis(endDate) }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val chosenDate = utcMillisToDateString(millis)
                            endDate = chosenDate
                            if (startDate > chosenDate) {
                                startDate = chosenDate
                            }
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.btn_ok), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }

    // Live Duration Calculation
    val calculatedDuration = remember(startDate, endDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)
            if (start != null && end != null && !end.before(start)) {
                val diff = (end.time - start.time) / (1000 * 60 * 60 * 24) + 1
                diff.toInt()
            } else 1
        } catch (e: Exception) {
            1
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.resetSaveSuccess()
            onSavedSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DevotionalHeader(
                title = if (visitId == null) stringResource(R.string.ashram_new_visit) else "Edit Ashram Visit",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Ashram Dropdown Card
            DevotionalCard {
                Text(
                    text = stringResource(R.string.ashram_select),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(8.dp))

                var ashramExpanded by remember { mutableStateOf(false) }
                val ashramOptions = uiState.masterData?.ashrams ?: emptyList()
                val currentAshramLabel = ashramOptions.find { it.code == selectedAshramCode }?.labels?.en ?: selectedAshramCode

                ExposedDropdownMenuBox(
                    expanded = ashramExpanded,
                    onExpandedChange = { ashramExpanded = !ashramExpanded }
                ) {
                    OutlinedTextField(
                        value = currentAshramLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ashramExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = ashramExpanded,
                        onDismissRequest = { ashramExpanded = false }
                    ) {
                        ashramOptions.forEach { ashram ->
                            DropdownMenuItem(
                                text = { Text("${ashram.labels.en} (${ashram.labels.hi})") },
                                onClick = {
                                    selectedAshramCode = ashram.code
                                    ashramExpanded = false
                                }
                            )
                        }
                    }
                }

                // If 'OTHER_ASHRAM' chosen -> show custom name field
                if (selectedAshramCode == "OTHER_ASHRAM") {
                    Spacer(modifier = Modifier.height(10.dp))
                    SevaTextField(
                        value = customAshramName,
                        onValueChange = { customAshramName = it },
                        label = stringResource(R.string.ashram_custom_name),
                        placeholder = "e.g. Satlok Ashram Shamli"
                    )
                }
            }

            // Dates & Duration Card
            DevotionalCard {
                Text(
                    text = "Visit Dates & Duration",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date Picker Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showStartDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(stringResource(R.string.start_date)) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Start Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // End Date Picker Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showEndDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text(stringResource(R.string.end_date)) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select End Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.duration_days),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = stringResource(R.string.days_count, calculatedDuration),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // Purpose & Conditional Seva Type Card
            DevotionalCard {
                Text(
                    text = "Purpose & Seva Details",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Purpose Dropdown
                var purposeExpanded by remember { mutableStateOf(false) }
                val purposeOptions = uiState.masterData?.purposes ?: emptyList()
                val currentPurposeLabel = purposeOptions.find { it.code == selectedPurpose }?.labels?.en ?: selectedPurpose

                ExposedDropdownMenuBox(
                    expanded = purposeExpanded,
                    onExpandedChange = { purposeExpanded = !purposeExpanded }
                ) {
                    OutlinedTextField(
                        value = currentPurposeLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.visit_purpose)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = purposeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = purposeExpanded,
                        onDismissRequest = { purposeExpanded = false }
                    ) {
                        purposeOptions.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.labels.en} (${p.labels.hi})") },
                                onClick = {
                                    selectedPurpose = p.code
                                    purposeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Conditional: Show Seva Type ONLY if purpose is 'SEVA'
                if (selectedPurpose == "SEVA") {
                    Spacer(modifier = Modifier.height(12.dp))
                    var sevaExpanded by remember { mutableStateOf(false) }
                    val sevaOptions = uiState.masterData?.sevaTypes ?: emptyList()
                    val currentSevaLabel = sevaOptions.find { it.code == selectedSevaType }?.labels?.en ?: selectedSevaType

                    ExposedDropdownMenuBox(
                        expanded = sevaExpanded,
                        onExpandedChange = { sevaExpanded = !sevaExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentSevaLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.seva_type)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sevaExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = sevaExpanded,
                            onDismissRequest = { sevaExpanded = false }
                        ) {
                            sevaOptions.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.labels.en} (${s.labels.hi})") },
                                    onClick = {
                                        selectedSevaType = s.code
                                        sevaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (selectedPurpose == "OTHER_PURPOSE" || (selectedPurpose == "SEVA" && selectedSevaType == "OTHER_SEVA")) {
                    Spacer(modifier = Modifier.height(10.dp))
                    SevaTextField(
                        value = customPurposeOrSeva,
                        onValueChange = { customPurposeOrSeva = it },
                        label = stringResource(R.string.custom_purpose_or_seva)
                    )
                }
            }

            // Notes & Location Details Card
            DevotionalCard {
                Text(
                    text = "Additional Memories (Optional)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.height(10.dp))

                SevaTextField(
                    value = locationDetails,
                    onValueChange = { locationDetails = it },
                    label = stringResource(R.string.location_details),
                    placeholder = "e.g. Bus from Delhi, stayed in hall 3"
                )

                Spacer(modifier = Modifier.height(10.dp))

                SevaTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = stringResource(R.string.notes_optional),
                    singleLine = false,
                    maxLines = 4,
                    placeholder = "Spiritual discourses attended, memorable seva experiences..."
                )
            }

            // Error Display
            val err = localError ?: uiState.errorMessage
            if (!err.isNullOrBlank()) {
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SevaPrimaryButton(
                text = stringResource(R.string.btn_save_visit),
                isLoading = uiState.isSaving,
                onClick = {
                    if (selectedAshramCode == "OTHER_ASHRAM" && customAshramName.isBlank()) {
                        localError = "Please specify the custom ashram name."
                    } else if (startDate.isBlank() || endDate.isBlank()) {
                        localError = "Start and End dates are required."
                    } else {
                        localError = null
                        viewModel.saveVisit(
                            recordId = visitId,
                            ashramCode = selectedAshramCode,
                            customAshramName = customAshramName,
                            startDate = startDate,
                            endDate = endDate,
                            purpose = selectedPurpose,
                            sevaType = selectedSevaType,
                            customPurposeOrSeva = customPurposeOrSeva,
                            notes = notes,
                            locationDetails = locationDetails
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

private fun parseDateToUtcMillis(dateStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun utcMillisToDateString(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(millis))
}
