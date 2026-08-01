package com.seend.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seend.app.ui.theme.DarkPrimaryBlue
import com.seend.app.ui.theme.PrimaryBlue
import com.seend.app.ui.theme.AccentBlue

@Composable
fun WelcomeScreen(
    onContinueClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryBlue,
                        DarkPrimaryBlue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(30.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Seend Logo",
                        modifier = Modifier.size(70.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Título
            Text(
                text = "Bienvenido a",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Text(
                text = "Seend",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontSize = 42.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtítulo
            Text(
                text = "Mensajería instantánea\nrápida y segura",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
        
        // Botón y términos en la parte inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Texto de términos
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)) {
                        append("Al continuar aceptas nuestros ")
                    }
                    withStyle(style = SpanStyle(color = AccentBlue, fontSize = 12.sp)) {
                        append("Términos y Condiciones")
                    }
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)) {
                        append(" del uso del servicio.")
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Botón Continuar
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryBlue
                )
            }
        }
    }
}
