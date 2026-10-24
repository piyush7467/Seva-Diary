package com.sevadiary.app.feature.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.network.dto.AddressDto
import com.sevadiary.app.core.network.dto.UpdateProfileRequest
import com.sevadiary.app.core.network.dto.UserProfileDto
import com.sevadiary.app.feature.seva.data.AshramColors
import com.sevadiary.app.feature.seva.data.rememberAshramColors
import com.sevadiary.app.feature.settings.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    devotionalName: String,
    currentLanguage: String,
    rawLanguage: String = currentLanguage,
    currentTheme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onOpenGuide: () -> Unit,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit = onBackClick,
    onNavigateToJaap: () -> Unit = {},
    onNavigateToVisits: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val colors = rememberAshramColors()
    val uiState by viewModel.uiState.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showPreferencesDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val defaultDevoteeStr = stringResource(R.string.devotee_title)
    val profile = uiState.userProfile ?: remember(devotionalName) {
        UserProfileDto(
            displayName = devotionalName.ifBlank { defaultDevoteeStr },
            username = "",
            email = "",
            phone = "",
            gender = "MALE",
            address = AddressDto(
                state = "",
                district = "",
                tehsil = "",
                cityVillage = "",
                addressLine = "",
                pinCode = ""
            ),
            mantraStage = "",
            mantraDetails = ""
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = colors.bg,
        bottomBar = {
            ProfileBottomBar(
                colors = colors,
                onHomeClick = onNavigateToHome,
                onJaapClick = onNavigateToJaap,
                onVisitsClick = onNavigateToVisits,
                onReportsClick = onNavigateToReports,
                onProfileClick = { /* Already on profile */ }
            )
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("OK", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Text(text = msg)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Subtle Loading Bar when refreshing profile in background
            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = colors.gold,
                    trackColor = colors.bannerBg
                )
            }

            // 1. Devotional Header Banner with Ashram Illustration & Avatar
            ProfileHeroSection(
                profile = profile,
                colors = colors,
                onEditClick = { showEditProfileDialog = true }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Content Sections in Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Personal Information
                PersonalInfoCard(
                    profile = profile,
                    colors = colors,
                    onClick = { showEditProfileDialog = true }
                )

                // Card 2: Address Information
                AddressInfoCard(
                    profile = profile,
                    colors = colors,
                    onClick = { showEditProfileDialog = true }
                )

                // Card 3: Mantra Details
                MantraDetailsCard(
                    profile = profile,
                    colors = colors,
                    onClick = { showEditProfileDialog = true }
                )

                // Card 4: Account & Security
                AccountSecurityCard(
                    colors = colors,
                    onChangePasswordClick = {
                        viewModel.resetPasswordState()
                        showChangePasswordDialog = true
                    },
                    onPreferencesClick = { showPreferencesDialog = true },
                    onGuideClick = onOpenGuide,
                    onLogoutClick = { showLogoutDialog = true },
                    onDeleteAccountClick = { showDeleteAccountDialog = true }
                )

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }

    // Dialog: Edit Profile
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentProfile = profile,
            colors = colors,
            isSaving = uiState.isSaving,
            onDismiss = { showEditProfileDialog = false },
            onSave = { updatedRequest ->
                viewModel.updateProfile(
                    request = updatedRequest,
                    onSuccess = {
                        showEditProfileDialog = false
                        snackbarMessage = "Profile updated successfully!"
                    },
                    onError = { err ->
                        snackbarMessage = err
                    }
                )
            }
        )
    }

    // Dialog: Change Password with 6-Digit OTP via Brevo
    if (showChangePasswordDialog) {
        ChangePasswordOtpDialog(
            userEmail = profile.email,
            colors = colors,
            uiState = uiState,
            onRequestOtp = {
                viewModel.requestChangePasswordOtp(
                    onSuccess = {
                        snackbarMessage = "Verification OTP sent to your email!"
                    },
                    onError = { err ->
                        snackbarMessage = err
                    }
                )
            },
            onVerifyOtp = { otp, newPassword ->
                viewModel.verifyChangePasswordOtp(
                    otp = otp,
                    newPassword = newPassword,
                    onSuccess = {
                        showChangePasswordDialog = false
                        snackbarMessage = "Password changed successfully!"
                    },
                    onError = { err ->
                        snackbarMessage = err
                    }
                )
            },
            onDismiss = {
                showChangePasswordDialog = false
                viewModel.resetPasswordState()
            }
        )
    }

    // Dialog: Theme & Language Preferences
    if (showPreferencesDialog) {
        ThemeLanguagePreferencesDialog(
            colors = colors,
            currentLanguage = currentLanguage,
            rawLanguage = rawLanguage,
            currentTheme = currentTheme,
            onLanguageChange = onLanguageChange,
            onThemeChange = onThemeChange,
            onDismiss = { showPreferencesDialog = false }
        )
    }

    // Dialog: Logout Confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.btn_logout),
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_confirm),
                    color = colors.secondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    }
                ) {
                    Text(stringResource(R.string.btn_confirm), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.btn_cancel), color = colors.primaryText)
                }
            },
            containerColor = colors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Fullscreen Overlay Loader: Signing Out
    if (uiState.isLoggingOut) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = {},
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = colors.primary,
                        strokeWidth = 3.dp
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.logging_out),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = colors.primaryText,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = stringResource(R.string.sat_saheb),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.gold,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }

    // Dialog: Delete Account Confirmation
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.delete_account_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_account_confirm),
                    color = colors.secondaryText
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.logout(onLogout)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text(stringResource(R.string.btn_cancel), color = colors.primaryText)
                }
            },
            containerColor = colors.cardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Hero Header matching reference: devotional landscape artwork background,
 * "My Profile" title, large framed circular avatar, name with gender symbol,
 * username, member badge, "Sat Saheb" greeting, and "Edit Profile" pill button.
 */
@Composable
private fun ProfileHeroSection(
    profile: UserProfileDto,
    colors: AshramColors,
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // Devotional illustrated background banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding1),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Warm devotional overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                (if (colors.isDark) Color(0x99141312) else Color(0x66F9F6EE)),
                                (if (colors.isDark) Color(0xFF141312) else Color(0xFFFAF8F2))
                            )
                        )
                    )
            )

            // Top Status & Title Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.my_profile_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.darkGreen,
                        fontSize = 24.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.my_profile_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.secondaryText,
                        fontSize = 12.5.sp
                    )
                )
            }
        }

        // Circular Avatar + Devotee Info Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 130.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Circular Portrait Avatar
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(colors.cardBg)
                    .border(BorderStroke(2.5.dp, colors.gold), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Centered Name, Username, Member Badge, Sat Saheb, and Edit Profile
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display Name + Gender Icon
                val genderSymbol = when (profile.gender.uppercase()) {
                    "FEMALE" -> "♀"
                    "MALE" -> "♂"
                    else -> "⚥"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val heroDisplayName = profile.displayName.ifBlank {
                        profile.firstName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.devotee_title)
                    }
                    Text(
                        text = heroDisplayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText,
                            fontSize = 21.sp
                        )
                    )
                    if (profile.gender.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = genderSymbol,
                            color = colors.primary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Username + Member Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (profile.username.isNotBlank()) {
                        val displayUsername = if (profile.username.startsWith("@")) profile.username else "@${profile.username}"
                        Text(
                            text = displayUsername,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (colors.isDark) Color(0xFF382914) else Color(0xFFFBEBD0)
                    ) {
                        Text(
                            text = stringResource(R.string.badge_member),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (colors.isDark) colors.gold else Color(0xFFB57410),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            ),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Sat Saheb greeting
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🪷",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.sat_saheb),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Edit Profile Button: Centered with its own row (Never overlaps with Member badge)
                Surface(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cardBg,
                    border = BorderStroke(1.dp, if (colors.isDark) colors.border else colors.primary.copy(alpha = 0.5f)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.profile_edit_btn),
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card 1: Personal Information with clean colon alignment.
 */
@Composable
private fun PersonalInfoCard(
    profile: UserProfileDto,
    colors: AshramColors,
    onClick: () -> Unit
) {
    ProfileSectionContainer(
        title = stringResource(R.string.personal_info_title),
        subtitle = stringResource(R.string.personal_info_subtitle),
        icon = Icons.Default.Person,
        iconBgColor = colors.badgeSageBg,
        iconTint = colors.primary,
        colors = colors,
        onHeaderClick = onClick
    ) {
        val defaultDevoteeStr = stringResource(R.string.devotee_title)
        val fullName = if (profile.firstName.isNullOrBlank() && profile.lastName.isNullOrBlank()) {
            profile.displayName.ifBlank { defaultDevoteeStr }
        } else {
            "${profile.firstName ?: ""} ${profile.lastName ?: ""}".trim().ifBlank { profile.displayName.ifBlank { defaultDevoteeStr } }
        }

        val genderText = when (profile.gender.uppercase()) {
            "FEMALE" -> "Female"
            "OTHER" -> "Other"
            else -> "Male"
        }

        ProfileDataRow(label = stringResource(R.string.label_full_name), value = fullName, colors = colors)
        ProfileDataRow(label = stringResource(R.string.label_gender), value = genderText, colors = colors)
        ProfileDataRow(label = stringResource(R.string.label_phone), value = profile.phone.ifBlank { "—" }, colors = colors)
        ProfileDataRow(label = stringResource(R.string.label_email), value = profile.email.ifBlank { "—" }, colors = colors)
        ProfileDataRow(label = stringResource(R.string.label_username), value = profile.username.ifBlank { "—" }, colors = colors)
    }
}

/**
 * Format address by joining present components: (if given) addressLine + village + tehsil + dist + state - pincode
 */
fun formatAddressString(address: AddressDto): String {
    val parts = mutableListOf<String>()
    if (address.addressLine.isNotBlank()) parts.add(address.addressLine.trim())
    if (address.cityVillage.isNotBlank()) parts.add(address.cityVillage.trim())
    if (address.tehsil.isNotBlank()) parts.add(address.tehsil.trim())
    if (address.district.isNotBlank()) parts.add(address.district.trim())
    if (address.state.isNotBlank()) parts.add(address.state.trim())

    val base = parts.joinToString(", ")
    return if (base.isNotBlank()) {
        if (address.pinCode.isNotBlank()) "$base - ${address.pinCode.trim()}" else base
    } else {
        "—"
    }
}

/**
 * Card 2: Address Information with clean colon alignment.
 */
@Composable
private fun AddressInfoCard(
    profile: UserProfileDto,
    colors: AshramColors,
    onClick: () -> Unit
) {
    val address = profile.address
    val formattedAddress = remember(address) { formatAddressString(address) }

    ProfileSectionContainer(
        title = stringResource(R.string.address_info_title),
        subtitle = stringResource(R.string.address_info_subtitle),
        icon = Icons.Default.Place,
        iconBgColor = if (colors.isDark) Color(0xFF382914) else Color(0xFFFEF0DE),
        iconTint = colors.gold,
        colors = colors,
        onHeaderClick = onClick
    ) {
        // Formatted Address: (if given) addressLine + village + tehsil + dist + state - pincode
        ProfileDataRow(
            label = stringResource(R.string.label_full_address),
            value = formattedAddress,
            colors = colors
        )

        // Address components (showing actual user data or "—")
        if (address.addressLine.isNotBlank()) {
            ProfileDataRow(
                label = stringResource(R.string.label_address),
                value = address.addressLine,
                colors = colors
            )
        }
        ProfileDataRow(
            label = stringResource(R.string.label_city_village),
            value = address.cityVillage.ifBlank { "—" },
            colors = colors
        )
        ProfileDataRow(
            label = stringResource(R.string.label_tehsil),
            value = address.tehsil.ifBlank { "—" },
            colors = colors
        )
        ProfileDataRow(
            label = stringResource(R.string.label_district),
            value = address.district.ifBlank { "—" },
            colors = colors
        )
        ProfileDataRow(
            label = stringResource(R.string.label_state),
            value = address.state.ifBlank { "—" },
            colors = colors
        )
        if (address.pinCode.isNotBlank()) {
            ProfileDataRow(
                label = stringResource(R.string.label_pin_code),
                value = address.pinCode,
                colors = colors
            )
        }
    }
}

/**
 * Card 3: Mantra Details with clean colon alignment.
 */
@Composable
private fun MantraDetailsCard(
    profile: UserProfileDto,
    colors: AshramColors,
    onClick: () -> Unit
) {
    ProfileSectionContainer(
        title = stringResource(R.string.mantra_details_title),
        subtitle = stringResource(R.string.mantra_details_subtitle),
        icon = null,
        customIcon = {
            Text(
                text = "ॐ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.gold,
                    fontSize = 18.sp
                )
            )
        },
        iconBgColor = if (colors.isDark) Color(0xFF382312) else Color(0xFFFDF0DF),
        iconTint = colors.gold,
        colors = colors,
        onHeaderClick = onClick
    ) {
        ProfileDataRow(
            label = stringResource(R.string.label_mantra_stage),
            value = profile.mantraStage.ifBlank { "—" },
            colors = colors
        )
        ProfileDataRow(
            label = stringResource(R.string.label_mantra_details),
            value = profile.mantraDetails.ifBlank { "—" },
            colors = colors
        )
    }
}

/**
 * Card 4: Account & Security: Change Password, Preferences, Logout, Delete Account.
 */
@Composable
private fun AccountSecurityCard(
    colors: AshramColors,
    onChangePasswordClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onGuideClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(colors.badgeSageBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.account_security_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.darkGreen,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.account_security_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.secondaryText,
                            fontSize = 12.sp
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.secondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = colors.border.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // Action 1: Change Password
            AccountActionRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.change_password_title),
                colors = colors,
                onClick = onChangePasswordClick
            )

            // Action 2: Theme & Language Preferences
            AccountActionRow(
                icon = Icons.Default.Palette,
                title = "Theme & Language",
                colors = colors,
                onClick = onPreferencesClick
            )

            // Action 3: App Walkthrough Guide
            AccountActionRow(
                icon = Icons.Default.Tour,
                title = "App Feature Walkthrough",
                colors = colors,
                onClick = onGuideClick
            )

            // Action 4: Logout
            AccountActionRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = stringResource(R.string.btn_logout),
                colors = colors,
                onClick = onLogoutClick
            )

            // Action 5: Delete Account
//            AccountActionRow(
//                icon = Icons.Default.Delete,
//                title = stringResource(R.string.delete_account_title),
//                textColor = MaterialTheme.colorScheme.error,
//                iconTint = MaterialTheme.colorScheme.error,
//                colors = colors,
//                onClick = onDeleteAccountClick
//            )
        }
    }
}

/**
 * Reusable row for Account & Security list.
 */
@Composable
private fun AccountActionRow(
    icon: ImageVector,
    title: String,
    colors: AshramColors,
    textColor: Color = colors.primaryText,
    iconTint: Color = colors.primaryText,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = textColor,
                fontSize = 14.sp
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Reusable Section Container for Personal, Address, and Mantra cards.
 */
@Composable
private fun ProfileSectionContainer(
    title: String,
    subtitle: String,
    icon: ImageVector?,
    customIcon: (@Composable () -> Unit)? = null,
    iconBgColor: Color,
    iconTint: Color,
    colors: AshramColors,
    onHeaderClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (customIcon != null) {
                        customIcon()
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.darkGreen,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.secondaryText,
                            fontSize = 12.sp
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.secondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Key-Value Rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

/**
 * Key-Value row with aligned colon and value matching the design reference.
 */
@Composable
private fun ProfileDataRow(
    label: String,
    value: String,
    colors: AshramColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.secondaryText,
                fontSize = 13.5.sp
            ),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.secondaryText,
                fontSize = 13.5.sp
            ),
            modifier = Modifier.width(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.primaryText,
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Change Password Dialog with 6-Digit OTP Verification via Brevo.
 */
@Composable
private fun ChangePasswordOtpDialog(
    userEmail: String,
    colors: AshramColors,
    uiState: com.sevadiary.app.feature.settings.presentation.SettingsUiState,
    onRequestOtp: () -> Unit,
    onVerifyOtp: (otp: String, newPassword: String) -> Unit,
    onDismiss: () -> Unit
) {
    var otpInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.badgeSageBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.otp_dialog_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText,
                        fontSize = 18.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!uiState.otpSentSuccess) {
                    // Step 1: Send OTP
                    Text(
                        text = stringResource(R.string.otp_dialog_desc),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.secondaryText,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.buttonSubtleBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Registered Email: $userEmail",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = colors.primary,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    if (uiState.isRequestingOtp) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    } else {
                        Button(
                            onClick = onRequestOtp,
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_send_otp),
                                color = colors.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Step 2: Enter 6-digit OTP & New Password
                    val targetEmail = uiState.maskedEmail ?: userEmail
                    Text(
                        text = stringResource(R.string.otp_sent_to, targetEmail),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                    )

                    // 6-digit OTP Field
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpInput = it },
                        label = { Text(stringResource(R.string.enter_6_digit_otp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.primaryText,
                            unfocusedTextColor = colors.primaryText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // New Password Field
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text(stringResource(R.string.enter_new_password)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = colors.secondaryText
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.primaryText,
                            unfocusedTextColor = colors.primaryText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Confirm New Password Field
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text(stringResource(R.string.confirm_new_password)) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.primaryText,
                            unfocusedTextColor = colors.primaryText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Resend OTP link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onRequestOtp) {
                            Text(stringResource(R.string.btn_resend_otp), color = colors.primary, fontSize = 12.5.sp)
                        }
                    }

                    if (localError != null || uiState.errorMessage != null) {
                        Text(
                            text = localError ?: uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }

                    if (uiState.isVerifyingOtp) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.primary)
                        }
                    } else {
                        Button(
                            onClick = {
                                localError = when {
                                    otpInput.length != 6 -> "Please enter the 6-digit OTP."
                                    newPasswordInput.length < 6 -> "New password must be at least 6 characters."
                                    newPasswordInput != confirmPasswordInput -> "Passwords do not match."
                                    else -> null
                                }
                                if (localError == null) {
                                    onVerifyOtp(otpInput, newPasswordInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.btn_update_password),
                                color = colors.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = colors.primaryText)
            }
        },
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(18.dp)
    )
}

/**
 * Edit Profile Modal dialog allowing complete updates for personal details, address, and mantra stage.
 */
@Composable
private fun EditProfileDialog(
    currentProfile: UserProfileDto,
    colors: AshramColors,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (UpdateProfileRequest) -> Unit
) {
    var firstName by remember { mutableStateOf(currentProfile.firstName ?: currentProfile.displayName) }
    var lastName by remember { mutableStateOf(currentProfile.lastName ?: "") }
    var gender by remember { mutableStateOf(currentProfile.gender) }
    var phone by remember { mutableStateOf(currentProfile.phone) }

    var state by remember { mutableStateOf(currentProfile.address.state) }
    var district by remember { mutableStateOf(currentProfile.address.district) }
    var tehsil by remember { mutableStateOf(currentProfile.address.tehsil) }
    var cityVillage by remember { mutableStateOf(currentProfile.address.cityVillage) }
    var addressLine by remember { mutableStateOf(currentProfile.address.addressLine) }
    var pinCode by remember { mutableStateOf(currentProfile.address.pinCode) }

    var mantraStage by remember { mutableStateOf(currentProfile.mantraStage) }
    var mantraDetails by remember { mutableStateOf(currentProfile.mantraDetails) }

    val genderOptions = listOf("MALE" to "Male", "FEMALE" to "Female", "OTHER" to "Other")
    val mantraStageOptions = listOf("Pratham Nam", "Satnam", "Sarnam")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.profile_edit_btn),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText,
                    fontSize = 19.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Personal Information",
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    fontSize = 13.5.sp
                )

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name / Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Gender Chips
                Text("Gender", color = colors.secondaryText, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genderOptions.forEach { (code, label) ->
                        FilterChip(
                            selected = gender.equals(code, ignoreCase = true),
                            onClick = { gender = code },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            )
                        )
                    }
                }

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.label_phone)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Address Information",
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    fontSize = 13.5.sp
                )

                // Address Line (Optional / if given)
                OutlinedTextField(
                    value = addressLine,
                    onValueChange = { addressLine = it },
                    label = { Text(stringResource(R.string.label_address)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // City / Village
                OutlinedTextField(
                    value = cityVillage,
                    onValueChange = { cityVillage = it },
                    label = { Text(stringResource(R.string.label_city_village)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Tehsil
                OutlinedTextField(
                    value = tehsil,
                    onValueChange = { tehsil = it },
                    label = { Text(stringResource(R.string.label_tehsil)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // District
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text(stringResource(R.string.label_district)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // State
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text(stringResource(R.string.label_state)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Pin Code
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { pinCode = it },
                    label = { Text(stringResource(R.string.label_pin_code)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Mantra Details",
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    fontSize = 13.5.sp
                )

                // Mantra Stage Chips
                Text("Mantra Stage", color = colors.secondaryText, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    mantraStageOptions.forEach { stage ->
                        FilterChip(
                            selected = mantraStage.equals(stage, ignoreCase = true),
                            onClick = { mantraStage = stage },
                            label = { Text(stage, fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            )
                        )
                    }
                }

                // Mantra Details Text
                OutlinedTextField(
                    value = mantraDetails,
                    onValueChange = { mantraDetails = it },
                    label = { Text(stringResource(R.string.label_mantra_details)) },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val request = UpdateProfileRequest(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        displayName = "${firstName.trim()} ${lastName.trim()}".trim(),
                        gender = gender,
                        phone = phone.trim(),
                        address = AddressDto(
                            state = state.trim(),
                            district = district.trim(),
                            tehsil = tehsil.trim(),
                            cityVillage = cityVillage.trim(),
                            addressLine = addressLine.trim(),
                            pinCode = pinCode.trim()
                        ),
                        mantraStage = mantraStage,
                        mantraDetails = mantraDetails.trim()
                    )
                    onSave(request)
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(18.dp))
                } else {
                    Text(stringResource(R.string.btn_save_changes), color = colors.onPrimary, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel), color = colors.primaryText)
            }
        },
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(18.dp)
    )
}

/**
 * Preferences Dialog: Theme & Language selectors with full dynamic preview.
 */
@Composable
private fun ThemeLanguagePreferencesDialog(
    colors: AshramColors,
    currentLanguage: String,
    rawLanguage: String,
    currentTheme: String,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText,
                    fontSize = 18.sp
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Language
                Text(
                    text = stringResource(R.string.pref_language),
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = rawLanguage == "en",
                        onClick = { onLanguageChange("en") },
                        label = { Text("English", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = rawLanguage == "hi",
                        onClick = { onLanguageChange("hi") },
                        label = { Text("हिंदी", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = rawLanguage == "system",
                        onClick = { onLanguageChange("system") },
                        label = { Text(stringResource(R.string.lang_system), fontSize = 12.sp) },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                HorizontalDivider(color = colors.border.copy(alpha = 0.5f))

                // Theme
                Text(
                    text = stringResource(R.string.pref_theme),
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = currentTheme == "light",
                        onClick = { onThemeChange("light") },
                        label = { Text(stringResource(R.string.theme_light), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = currentTheme == "dark",
                        onClick = { onThemeChange("dark") },
                        label = { Text(stringResource(R.string.theme_dark), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = currentTheme == "system",
                        onClick = { onThemeChange("system") },
                        label = { Text(stringResource(R.string.theme_system), fontSize = 12.sp) },
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", color = colors.onPrimary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.cardBg,
        shape = RoundedCornerShape(18.dp)
    )
}

/**
 * 5-item Bottom Navigation Bar with Profile selected.
 */
@Composable
private fun ProfileBottomBar(
    colors: AshramColors,
    onHomeClick: () -> Unit,
    onJaapClick: () -> Unit,
    onVisitsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.cardBg,
        border = BorderStroke(1.dp, colors.border),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                label = stringResource(R.string.nav_home),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.nav_home),
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = onHomeClick
            )

            // 2. Naam Jaap
            BottomNavItem(
                label = stringResource(R.string.nav_jaap),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        painter = painterResource(R.drawable.ic_devotional_jaap_mala),
                        contentDescription = stringResource(R.string.nav_jaap),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onJaapClick
            )

            // 3. Visits
            BottomNavItem(
                label = stringResource(R.string.nav_visits),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        painter = painterResource(R.drawable.ic_devotional_ashram),
                        contentDescription = stringResource(R.string.nav_visits),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onVisitsClick
            )

            // 4. Reports
            BottomNavItem(
                label = stringResource(R.string.nav_reports),
                selected = false,
                colors = colors,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = stringResource(R.string.nav_reports),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onReportsClick
            )

            // 5. Profile (Active)
            BottomNavItem(
                label = stringResource(R.string.nav_profile),
                selected = true,
                colors = colors,
                icon = { tint ->
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.nav_profile),
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    colors: AshramColors,
    icon: @Composable (tint: Color) -> Unit,
    onClick: () -> Unit
) {
    val activeColor = colors.primary
    val inactiveColor = if (colors.isDark) Color(0xFF8E8880) else Color(0xFF8E9690)
    val color = if (selected) activeColor else inactiveColor

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(color)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = color,
                fontSize = 11.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        if (selected) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(activeColor)
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
