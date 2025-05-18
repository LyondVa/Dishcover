package com.nhatpham.dishcover.presentation.auth.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = state.isPasswordResetComplete) {
        if (state.isPasswordResetComplete) {
            onNavigateToLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when (state.currentStep) {
                PasswordResetStep.EMAIL_STEP -> "Reset Password"
                PasswordResetStep.VERIFICATION_STEP -> "Verify Code"
                PasswordResetStep.NEW_PASSWORD_STEP -> "Set New Password"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (state.currentStep) {
            PasswordResetStep.EMAIL_STEP -> {
                EmailStep(
                    state = state,
                    onEmailChanged = { viewModel.onEvent(ForgotPasswordEvent.EmailChanged(it)) },
                    onSubmit = { viewModel.onEvent(ForgotPasswordEvent.RequestPasswordReset) },
                    focusManager = focusManager
                )
            }
            PasswordResetStep.VERIFICATION_STEP -> {
                VerificationStep(
                    state = state,
                    onCodeChanged = { viewModel.onEvent(ForgotPasswordEvent.VerificationCodeChanged(it)) },
                    onSubmit = { viewModel.onEvent(ForgotPasswordEvent.VerifyCode) },
                    onBackToEmail = { viewModel.onEvent(ForgotPasswordEvent.BackToEmailStep) },
                    focusManager = focusManager
                )
            }
            PasswordResetStep.NEW_PASSWORD_STEP -> {
                NewPasswordStep(
                    state = state,
                    onNewPasswordChanged = { viewModel.onEvent(ForgotPasswordEvent.NewPasswordChanged(it)) },
                    onConfirmPasswordChanged = { viewModel.onEvent(ForgotPasswordEvent.ConfirmNewPasswordChanged(it)) },
                    onTogglePasswordVisibility = { viewModel.onEvent(ForgotPasswordEvent.TogglePasswordVisibility) },
                    onSubmit = { viewModel.onEvent(ForgotPasswordEvent.SubmitNewPassword) },
                    focusManager = focusManager
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        state.error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun EmailStep(
    state: ForgotPasswordState,
    onEmailChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Enter your email address and we'll send you a verification code to reset your password.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChanged,
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email"
                )
            },
            isError = state.emailError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.emailError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Send Verification Code")
            }
        }

        if (state.isResetEmailSent) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Verification Code Sent",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Check your email for a verification code. If it doesn't appear within a few minutes, check your spam folder.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationStep(
    state: ForgotPasswordState,
    onCodeChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToEmail: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Enter the verification code that was sent to ${state.email}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.verificationCode,
            onValueChange = onCodeChanged,
            label = { Text("Verification Code") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "Verification Code"
                )
            },
            isError = state.verificationCodeError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.verificationCodeError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Verify Code")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBackToEmail,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Didn't receive a code? Try again")
        }
    }
}

@Composable
fun NewPasswordStep(
    state: ForgotPasswordState,
    onNewPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Create a new password for ${state.verifiedEmail}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.newPassword,
            onValueChange = onNewPasswordChanged,
            label = { Text("New Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "New Password"
                )
            },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.newPasswordError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.newPasswordError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.confirmNewPassword,
            onValueChange = onConfirmPasswordChanged,
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Password"
                )
            },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = state.confirmNewPasswordError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.confirmNewPasswordError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Reset Password")
            }
        }
    }
}