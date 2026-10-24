package com.sevadiary.app.feature.auth

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.SevaPasswordField
import com.sevadiary.app.core.designsystem.component.SevaPrimaryButton
import com.sevadiary.app.core.designsystem.component.SevaTextField
import com.sevadiary.app.feature.auth.presentation.AuthViewModel
import com.sevadiary.app.feature.seva.data.rememberAshramColors

private enum class ForgotPasswordStep {
    ENTER_EMAIL,
    ENTER_OTP_AND_PASSWORD,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val colors = rememberAshramColors()
    val focusManager = LocalFocusManager.current

    var currentStep by remember { mutableStateOf(ForgotPasswordStep.ENTER_EMAIL) }
    var email by remember { mutableStateOf("") }
    var maskedEmail by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val genericErrorStr = stringResource(R.string.error_occurred)
    val invalidEmailStr = stringResource(R.string.email_address)
    val otpLengthErrorStr = stringResource(R.string.otp_length_error)
    val passwordLengthErrorStr = stringResource(R.string.password_length_error)
    val passwordMismatchErrorStr = stringResource(R.string.password_mismatch_error)

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.forgot_pw_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = colors.darkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.bg,
                    titleContentColor = colors.primaryText,
                    navigationIconContentColor = colors.darkGreen
                )
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
                .padding(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Step 1: Request Reset OTP
            if (currentStep == ForgotPasswordStep.ENTER_EMAIL) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.badgeSageBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.forgot_pw_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.sat_saheb),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colors.gold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.forgot_pw_desc),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.secondaryText,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Error Banner
                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // Email input
                SevaTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = stringResource(R.string.email_address),
                    placeholder = "devotee@example.com",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )

                // Send OTP Button
                SevaPrimaryButton(
                    text = stringResource(R.string.btn_send_code),
                    isLoading = isLoading,
                    enabled = email.trim().isNotBlank() && !isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        val cleanEmail = email.trim().lowercase()
                        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                            errorMessage = "$invalidEmailStr: Please enter a valid email."
                            return@SevaPrimaryButton
                        }

                        if (viewModel == null) {
                            errorMessage = genericErrorStr
                            return@SevaPrimaryButton
                        }

                        isLoading = true
                        errorMessage = null
                        infoMessage = null

                        viewModel.requestPasswordReset(
                            email = cleanEmail,
                            onSuccess = { data ->
                                isLoading = false
                                maskedEmail = data.maskedEmail ?: cleanEmail
                                currentStep = ForgotPasswordStep.ENTER_OTP_AND_PASSWORD
                                errorMessage = null
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    }
                )

                // Back to login button
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.already_have_account),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // Step 2: Enter 6-digit OTP & New Password
            else if (currentStep == ForgotPasswordStep.ENTER_OTP_AND_PASSWORD) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.bannerBg),
                    border = BorderStroke(1.dp, colors.bannerBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.enter_6_digit_otp),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                        )
                        Text(
                            text = stringResource(R.string.otp_sent_to, maskedEmail.ifBlank { email }),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.secondaryText,
                                lineHeight = 18.sp
                            )
                        )
                        Text(
                            text = stringResource(R.string.forgot_pw_step2_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Success or Error notifications
                AnimatedVisibility(
                    visible = !infoMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.badgeSageBg
                        ),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = infoMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // 6-digit OTP field
                SevaTextField(
                    value = otp,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 6) {
                            otp = digits
                            errorMessage = null
                        }
                    },
                    label = stringResource(R.string.enter_6_digit_otp),
                    placeholder = "123456",
                    leadingIcon = Icons.Default.Lock,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )

                // New password
                SevaPasswordField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = stringResource(R.string.enter_new_password),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }
                    )
                )

                // Confirm new password
                SevaPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = stringResource(R.string.confirm_new_password),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )

                // Resend OTP row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wrong email?",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.secondaryText),
                        modifier = Modifier
                            .clickable {
                                currentStep = ForgotPasswordStep.ENTER_EMAIL
                                errorMessage = null
                                infoMessage = null
                            }
                            .padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isLoading) {
                                if (viewModel == null) return@clickable
                                isLoading = true
                                errorMessage = null
                                infoMessage = null
                                viewModel.requestPasswordReset(
                                    email = email.trim().lowercase(),
                                    onSuccess = {
                                        isLoading = false
                                        infoMessage = "A fresh 6-digit code has been sent."
                                    },
                                    onError = { err ->
                                        isLoading = false
                                        errorMessage = err
                                    }
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.btn_resend_otp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Submit Reset Password
                SevaPrimaryButton(
                    text = stringResource(R.string.btn_update_password),
                    isLoading = isLoading,
                    enabled = otp.length == 6 && newPassword.isNotBlank() && confirmPassword.isNotBlank() && !isLoading,
                    onClick = {
                        focusManager.clearFocus()
                        if (otp.length != 6) {
                            errorMessage = otpLengthErrorStr
                            return@SevaPrimaryButton
                        }
                        if (newPassword.length < 6) {
                            errorMessage = passwordLengthErrorStr
                            return@SevaPrimaryButton
                        }
                        if (newPassword != confirmPassword) {
                            errorMessage = passwordMismatchErrorStr
                            return@SevaPrimaryButton
                        }

                        if (viewModel == null) {
                            errorMessage = genericErrorStr
                            return@SevaPrimaryButton
                        }

                        isLoading = true
                        errorMessage = null
                        infoMessage = null

                        viewModel.resetPasswordWithOtp(
                            email = email.trim().lowercase(),
                            otp = otp.trim(),
                            newPassword = newPassword,
                            onSuccess = {
                                isLoading = false
                                currentStep = ForgotPasswordStep.SUCCESS
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    }
                )
            }

            // Step 3: Success Screen
            else if (currentStep == ForgotPasswordStep.SUCCESS) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.cardBg),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(colors.badgeSageBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Text(
                            text = stringResource(R.string.password_changed_success),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText,
                                textAlign = TextAlign.Center
                            )
                        )

                        Text(
                            text = stringResource(R.string.forgot_pw_success),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                SevaPrimaryButton(
                    text = stringResource(R.string.btn_login),
                    onClick = onNavigateBack
                )
            }
        }
    }
}
