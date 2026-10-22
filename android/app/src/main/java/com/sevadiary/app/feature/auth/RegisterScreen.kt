package com.sevadiary.app.feature.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sevadiary.app.core.model.IndiaLocations
import com.sevadiary.app.feature.auth.presentation.AuthUiState
import com.sevadiary.app.feature.auth.presentation.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onRegistrationSuccess: (devotionalName: String) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Step 1: Personal Details
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("MALE") }
    var displayName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Step 2: Address & Mantra Details
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var tehsil by remember { mutableStateOf("") }
    var mantraStage by remember { mutableStateOf("Pratham Nam") }
    var cityVillage by remember { mutableStateOf("") }
    var addressLine by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var mantraDetails by remember { mutableStateOf("") }

    var showStatePicker by remember { mutableStateOf(false) }
    var showDistrictPicker by remember { mutableStateOf(false) }
    var tehsilExpanded by remember { mutableStateOf(false) }
    var mantraExpanded by remember { mutableStateOf(false) }
    var customTehsilMode by remember { mutableStateOf(false) }

    val allStates = remember { IndiaLocations.states }
    val currentDistricts = remember(state) {
        IndiaLocations.getDistrictsForState(state)
    }

    val mantraOptions = remember {
        listOf("Pratham Nam", "Satnam", "Sarnam")
    }

    val districtTehsils = remember {
        mapOf(
            "Hisar" to listOf("Barwala", "Hansi", "Hisar", "Narnaund", "Bass", "Other Tehsil"),
            "Rohtak" to listOf("Rohtak", "Sampla", "Meham", "Kalanaur", "Other Tehsil"),
            "Bhiwani" to listOf("Bhiwani", "Siwani", "Tosham", "Bawani Khera", "Loharu", "Other Tehsil"),
            "Sirsa" to listOf("Sirsa", "Dabwali", "Rania", "Ellenabad", "Kalanwali", "Other Tehsil"),
            "Fatehabad" to listOf("Fatehabad", "Tohana", "Ratia", "Bhattukalan", "Other Tehsil"),
            "Jind" to listOf("Jind", "Narwana", "Safidon", "Julana", "Uchana", "Alewa", "Other Tehsil"),
            "Sonipat" to listOf("Sonipat", "Gohana", "Ganaur", "Kharkhoda", "Rai", "Other Tehsil")
        )
    }

    // Step 3: Account Credentials
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    var localErrorMsg by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.uiState.collectAsState()

    // Auto-suggest devotional display name on name or gender change
    LaunchedEffect(firstName, gender) {
        if (firstName.isNotBlank()) {
            val suffix = if (gender == "FEMALE") "Dasi" else "Das"
            displayName = "${firstName.trim()} $suffix"
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.RegisterSuccess) {
            val user = (authState as AuthUiState.RegisterSuccess).user
            viewModel.resetState()
            onRegistrationSuccess(user.displayName)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.reg_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1) step -= 1 else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step Indicator
            Text(
                text = when (step) {
                    1 -> stringResource(R.string.reg_step_1)
                    2 -> stringResource(R.string.reg_step_2)
                    3 -> stringResource(R.string.reg_step_3)
                    else -> stringResource(R.string.reg_step_4)
                },
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )

            LinearProgressIndicator(
                progress = { step / 4f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (step) {
                1 -> {
                    SevaTextField(
                        value = firstName,
                        onValueChange = { firstName = it; localErrorMsg = null },
                        label = stringResource(R.string.first_name)
                    )
                    SevaTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = stringResource(R.string.last_name)
                    )

                    // Gender Selector
                    Text(
                        text = stringResource(R.string.gender),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (gender == "MALE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { gender = "MALE" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.gender_male),
                                color = if (gender == "MALE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (gender == "FEMALE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { gender = "FEMALE" }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.gender_female),
                                color = if (gender == "FEMALE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    SevaTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = stringResource(R.string.devotional_name)
                    )

                    SevaTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = stringResource(R.string.phone_number),
                        leadingIcon = Icons.Default.Phone
                    )

                    SevaTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.email_address),
                        leadingIcon = Icons.Default.Email
                    )
                }

                2 -> {
                    // 1. Searchable State Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.state_required),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showStatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = if (state.isNotBlank()) state else stringResource(R.string.select_state),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select State",
                                        tint = ForestGreen
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = if (state.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledTrailingIconColor = ForestGreen
                                )
                            )
                        }
                    }

                    // 2. Searchable District Field (Filtered by selected state)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.district_required),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (state.isBlank()) {
                                        localErrorMsg = "Please select a State first"
                                    } else {
                                        showDistrictPicker = true
                                    }
                                }
                        ) {
                            OutlinedTextField(
                                value = if (district.isNotBlank()) district else if (state.isBlank()) "Select State first" else stringResource(R.string.select_district),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select District",
                                        tint = if (state.isNotBlank()) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = if (district.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                                    disabledTrailingIconColor = if (state.isNotBlank()) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // 3. Tehsil Dropdown / Field
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.tehsil_required),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val availableTehsils = districtTehsils[district]
                        if (availableTehsils != null && !customTehsilMode) {
                            ExposedDropdownMenuBox(
                                expanded = tehsilExpanded,
                                onExpandedChange = { tehsilExpanded = !tehsilExpanded }
                            ) {
                                OutlinedTextField(
                                    value = if (tehsil.isNotBlank()) tehsil else stringResource(R.string.select_tehsil),
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tehsilExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ForestGreen,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = tehsilExpanded,
                                    onDismissRequest = { tehsilExpanded = false }
                                ) {
                                    availableTehsils.forEach { teh ->
                                        DropdownMenuItem(
                                            text = { Text(teh) },
                                            onClick = {
                                                if (teh == "Other Tehsil") {
                                                    tehsil = ""
                                                    customTehsilMode = true
                                                } else {
                                                    tehsil = teh
                                                }
                                                tehsilExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            SevaTextField(
                                value = tehsil,
                                onValueChange = { tehsil = it },
                                label = stringResource(R.string.select_tehsil),
                                placeholder = "Enter Tehsil"
                            )
                        }
                    }

                    // 4. Mantra * Dropdown (Pratham Nam / Satnam / Sarnam)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.mantra_required),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = mantraExpanded,
                            onExpandedChange = { mantraExpanded = !mantraExpanded }
                        ) {
                            OutlinedTextField(
                                value = if (mantraStage.isNotBlank()) mantraStage else stringResource(R.string.select_mantra),
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mantraExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ForestGreen,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = mantraExpanded,
                                onDismissRequest = { mantraExpanded = false }
                            ) {
                                mantraOptions.forEach { option ->
                                    val isSelected = (mantraStage == option)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) ForestGreen else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            mantraStage = option
                                            mantraExpanded = false
                                        },
                                        modifier = if (isSelected) Modifier.background(ForestGreen.copy(alpha = 0.1f)) else Modifier
                                    )
                                }
                            }
                        }
                    }

                    // Village / City
                    SevaTextField(
                        value = cityVillage,
                        onValueChange = { cityVillage = it },
                        label = stringResource(R.string.city_village),
                        placeholder = "e.g. Dhanaana / Barwala"
                    )

                    // Address Line & PIN Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SevaTextField(
                            value = addressLine,
                            onValueChange = { addressLine = it },
                            label = stringResource(R.string.address_line),
                            modifier = Modifier.weight(1.5f)
                        )
                        SevaTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it },
                            label = stringResource(R.string.pin_code),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Additional Spiritual Notes (Optional)
                    SevaTextField(
                        value = mantraDetails,
                        onValueChange = { mantraDetails = it },
                        label = stringResource(R.string.mantra_details_optional),
                        singleLine = false,
                        maxLines = 3
                    )
                }

                3 -> {
                    SevaTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = stringResource(R.string.username),
                        placeholder = "e.g. ramdas12"
                    )
                    Text(
                        text = stringResource(R.string.username_helper),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SevaPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.password)
                    )
                    SevaPasswordField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = stringResource(R.string.confirm_password)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { termsAccepted = !termsAccepted }
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it }
                        )
                        Text(
                            text = stringResource(R.string.terms_consent),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                4 -> {
                    DevotionalCard {
                        Text(
                            text = "Review Registration Details",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Devotional Name: $displayName", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary))
                        Text(text = "Mantra Initiation: $mantraStage", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = SaffronGold))
                        Text(text = "Full Name: $firstName $lastName", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Email: $email", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Phone: $phone", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "State / District: $state, $district", style = MaterialTheme.typography.bodyMedium)
                        if (tehsil.isNotBlank()) {
                            Text(text = "Tehsil: $tehsil", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (cityVillage.isNotBlank()) {
                            Text(text = "Village / City: $cityVillage", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(text = "Username: $username", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Display errors
            val errorText = localErrorMsg ?: if (authState is AuthUiState.Error) (authState as AuthUiState.Error).message else null
            if (!errorText.isNullOrBlank()) {
                Text(
                    text = errorText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isLoading = authState is AuthUiState.Loading

            SevaPrimaryButton(
                text = if (step < 4) stringResource(R.string.btn_continue) else stringResource(R.string.btn_create_account),
                isLoading = isLoading,
                onClick = {
                    when (step) {
                        1 -> {
                            if (firstName.isBlank()) {
                                localErrorMsg = "First name is required"
                            } else {
                                localErrorMsg = null
                                step = 2
                            }
                        }
                        2 -> {
                            if (state.isBlank()) {
                                localErrorMsg = "Please select or enter your State"
                            } else if (district.isBlank()) {
                                localErrorMsg = "Please select or enter your District"
                            } else if (mantraStage.isBlank()) {
                                localErrorMsg = "Please select your Mantra"
                            } else {
                                localErrorMsg = null
                                step = 3
                            }
                        }
                        3 -> {
                            if (username.length < 3 || username.length > 20) {
                                localErrorMsg = "Username must be 3–20 characters"
                            } else if (!username.all { it.isLetterOrDigit() }) {
                                localErrorMsg = "Username must contain only English letters and numbers"
                            } else if (password.length < 6) {
                                localErrorMsg = "Password must be at least 6 characters"
                            } else if (password != confirmPassword) {
                                localErrorMsg = "Passwords do not match"
                            } else if (!termsAccepted) {
                                localErrorMsg = "Please accept the terms to proceed"
                            } else {
                                localErrorMsg = null
                                step = 4
                            }
                        }
                        4 -> {
                            viewModel.register(
                                firstName = firstName,
                                lastName = lastName,
                                gender = gender,
                                displayName = displayName.ifBlank { "$firstName Das" },
                                username = username,
                                email = email,
                                phone = phone,
                                addressLine = addressLine,
                                cityVillage = cityVillage,
                                tehsil = tehsil,
                                district = district,
                                state = state,
                                pinCode = pinCode,
                                mantraStage = mantraStage,
                                mantraDetails = mantraDetails,
                                password = password
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(56.dp))
        }
    }

    if (showStatePicker) {
        SearchableListDialog(
            title = "Select State",
            searchPlaceholder = "Search state / UT...",
            items = allStates,
            selectedItem = state,
            onItemSelected = { selected ->
                state = selected
                district = ""
                tehsil = ""
                localErrorMsg = null
            },
            onDismissRequest = { showStatePicker = false }
        )
    }

    if (showDistrictPicker) {
        SearchableListDialog(
            title = "Select District in $state",
            searchPlaceholder = "Search district in $state...",
            items = currentDistricts,
            selectedItem = district,
            onItemSelected = { selected ->
                district = selected
                tehsil = ""
                localErrorMsg = null
            },
            onDismissRequest = { showDistrictPicker = false }
        )
    }
}

@Composable
fun SearchableListDialog(
    title: String,
    searchPlaceholder: String,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank()) items else items.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(searchPlaceholder, style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No matching district/state found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    onItemSelected(searchQuery.trim())
                                    onDismissRequest()
                                }) {
                                    Text("Use \"${searchQuery.trim()}\"", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredItems) { item ->
                            val isSelected = (item == selectedItem)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        onItemSelected(item)
                                        onDismissRequest()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.btn_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
