package com.sevadiary.app.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.*
import com.sevadiary.app.core.designsystem.theme.ForestGreen
import com.sevadiary.app.core.network.ApiClient
import com.sevadiary.app.feature.auth.presentation.AuthUiState
import com.sevadiary.app.feature.auth.presentation.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (devotionalName: String) -> Unit
) {
    var usernameOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(ApiClient.baseUrl) }

    val authState by viewModel.uiState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.LoginSuccess) {
            val user = (authState as AuthUiState.LoginSuccess).authData.user
            viewModel.resetState()
            onLoginSuccess(user.displayName)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Seva Diary Logo",
                    modifier = Modifier
                        .size(95.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Server URL indicator & configuration chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        serverUrlInput = ApiClient.baseUrl
                        showServerDialog = true
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Wifi,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = ForestGreen
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Server: ${ApiClient.baseUrl}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SevaTextField(
                value = usernameOrEmail,
                onValueChange = { usernameOrEmail = it; localError = null },
                label = stringResource(R.string.username_or_email),
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            SevaPasswordField(
                value = password,
                onValueChange = { password = it; localError = null },
                label = stringResource(R.string.password)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = stringResource(R.string.forgot_password),
                        color = ForestGreen,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            val errorMessage = localError ?: if (authState is AuthUiState.Error) (authState as AuthUiState.Error).message else null
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isLoading = authState is AuthUiState.Loading

            SevaPrimaryButton(
                text = stringResource(R.string.btn_login),
                onClick = {
                    if (usernameOrEmail.isBlank() || password.isBlank()) {
                        localError = "Please enter both credentials"
                    } else {
                        viewModel.login(usernameOrEmail, password)
                    }
                },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.no_account),
                color = ForestGreen,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.clickable(onClick = onNavigateToRegister)
            )
        }
    }

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Server Connection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Select your network mode or enter backend address:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = serverUrlInput.contains("vercel.app"),
                            onClick = { serverUrlInput = ApiClient.CLOUD_URL },
                            label = { Text("Cloud (Vercel)", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = serverUrlInput.contains("127.0.0.1"),
                            onClick = { serverUrlInput = ApiClient.USB_URL },
                            label = { Text("USB", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = serverUrlInput.contains("10.35.29.227"),
                            onClick = { serverUrlInput = ApiClient.WIFI_URL },
                            label = { Text("Wi-Fi", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = serverUrlInput.contains("10.0.2.2"),
                            onClick = { serverUrlInput = ApiClient.EMULATOR_URL },
                            label = { Text("Emulator", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    ApiClient.updateHost(serverUrlInput)
                    showServerDialog = false
                }) {
                    Text("Apply", color = ForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
