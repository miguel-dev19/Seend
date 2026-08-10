package com.seend.app.ui.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.seend.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, Uri?) -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedImageUri = it }
    }

    Box(modifier = Modifier.fillMaxSize().background(White)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp).padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Volver", tint = Black) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Crear cuenta", style = MaterialTheme.typography.headlineLarge, color = Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Únete a Seend y comienza\na chatear con tus amigos", style = MaterialTheme.typography.bodyLarge, color = Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            // Avatar
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.size(120.dp)) {
                Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), shape = CircleShape, color = LightBlue) {
                    if (selectedImageUri != null) {
                        AsyncImage(model = selectedImageUri, contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Outlined.Person, "Avatar", modifier = Modifier.size(70.dp), tint = PrimaryBlue)
                        }
                    }
                }
                FloatingActionButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.size(40.dp), shape = CircleShape,
                    containerColor = PrimaryBlue, contentColor = White
                ) { Icon(Icons.Default.CameraAlt, "Foto", modifier = Modifier.size(20.dp)) }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Campo Nombre - icono de usuario
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Outlined.Person, "Nombre", tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = LightGray, focusedLabelColor = PrimaryBlue, unfocusedLabelColor = Gray, cursorColor = PrimaryBlue, focusedTextColor = Black, unfocusedTextColor = Black),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo Usuario - icono @
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Usuario") },
                leadingIcon = { Icon(Icons.Outlined.AlternateEmail, "@", tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = LightGray, focusedLabelColor = PrimaryBlue, unfocusedLabelColor = Gray, cursorColor = PrimaryBlue, focusedTextColor = Black, unfocusedTextColor = Black),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, "Contraseña", tint = PrimaryBlue) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Mostrar", tint = Gray)
                    }
                },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue, unfocusedBorderColor = LightGray, focusedLabelColor = PrimaryBlue, unfocusedLabelColor = Gray, cursorColor = PrimaryBlue, focusedTextColor = Black, unfocusedTextColor = Black),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onRegisterClick(name, username, password, selectedImageUri) },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = !isLoading && name.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
                else Text("Crear cuenta", style = MaterialTheme.typography.titleMedium, color = White)
            }
        }
    }
}
