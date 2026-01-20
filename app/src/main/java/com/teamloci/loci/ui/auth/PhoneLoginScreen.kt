package com.teamloci.loci.ui.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneLoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNeedSignUp: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()

    val context = LocalContext.current
    var countryCode by remember { mutableStateOf("+82") }
    var showCountryPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.NeedSignUp -> onNeedSignUp()
            else -> {}
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("✕", color = Color.White, fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            val titleText = if (uiState is AuthUiState.CodeSent) "Enter verification code" else "Verify your phone number"
            val subText = if (uiState is AuthUiState.CodeSent) "We sent a code to $countryCode $phoneNumber" else "Your number helps keep your account safe."

            Text(
                text = titleText,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subText,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState is AuthUiState.CodeSent) {
                TextField(
                    value = verificationCode,
                    onValueChange = { if (it.length <= 6) viewModel.verificationCode.value = it },
                    placeholder = { Text("123456", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showCountryPicker = true }
                            .padding(horizontal = 16.dp, vertical = 18.dp)
                    ) {
                        Text(text = "🇰🇷 $countryCode", color = Color.White, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 15) viewModel.phoneNumber.value = it },
                        placeholder = { Text("010 0000 0000", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Transparent),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
            }

            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val isButtonEnabled = if (uiState is AuthUiState.CodeSent) verificationCode.length == 6 else phoneNumber.length >= 10
            val buttonText = if (uiState is AuthUiState.CodeSent) "Verify" else "Send verification code"

            Button(
                onClick = {
                    if (uiState is AuthUiState.CodeSent) {
                        viewModel.verifyCode()
                    } else {
                        if (context is Activity) {
                            val fullNumber = "$countryCode${phoneNumber.replaceFirst("0", "")}".replace(" ", "")
                            viewModel.phoneNumber.value = fullNumber
                            viewModel.sendVerificationCode(context)
                        }
                    }
                },
                enabled = isButtonEnabled && uiState !is AuthUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(buttonText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (showCountryPicker) {
            AlertDialog(
                onDismissRequest = { showCountryPicker = false },
                title = { Text("Select Country Code") },
                text = {
                    Column {
                        Text("🇰🇷 Korea (+82)", Modifier.clickable { countryCode = "+82"; showCountryPicker = false }.padding(12.dp))
                        Text("🇺🇸 USA (+1)", Modifier.clickable { countryCode = "+1"; showCountryPicker = false }.padding(12.dp))
                        Text("🇯🇵 Japan (+81)", Modifier.clickable { countryCode = "+81"; showCountryPicker = false }.padding(12.dp))
                    }
                },
                confirmButton = {}
            )
        }
    }
}