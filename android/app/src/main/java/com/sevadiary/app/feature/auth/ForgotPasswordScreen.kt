package com.sevadiary.app.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sevadiary.app.R
import com.sevadiary.app.core.designsystem.component.DevotionalCard
import com.sevadiary.app.core.designsystem.component.SevaPrimaryButton
import com.sevadiary.app.core.designsystem.component.SevaTextField
import com.sevadiary.app.core.designsystem.theme.ForestGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.forgot_pw_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isSubmitted) {
                DevotionalCard {
                    Text(
                        text = stringResource(R.string.reset_link_sent_msg),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                SevaPrimaryButton(
                    text = stringResource(R.string.btn_back),
                    onClick = onNavigateBack
                )
                Spacer(modifier = Modifier.height(48.dp))
            } else {
                Text(
                    text = stringResource(R.string.forgot_pw_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                SevaTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.email_address),
                    leadingIcon = Icons.Default.Email
                )

                Spacer(modifier = Modifier.height(12.dp))

                SevaPrimaryButton(
                    text = stringResource(R.string.btn_send_reset_link),
                    onClick = {
                        if (email.isNotBlank()) {
                            isSubmitted = true
                        }
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
