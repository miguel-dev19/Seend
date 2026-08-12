package com.seend.app.ui.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seend.app.ui.theme.*
import com.seend.app.util.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalChatScreen(
    viewModel: GlobalChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val typingText = viewModel.getTypingText()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.size - 1)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Seend Global", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Black)
                            Text(
                                "${uiState.users.size} usuarios, ${uiState.onlineCount} en línea",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray,
                                fontSize = 12.sp
                            )
                            if (typingText.isNotEmpty()) {
                                Text(
                                    typingText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryBlue,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
                )
                Divider(color = LightGray, thickness = 0.5.dp)
            }
        },
        bottomBar = {
            Surface(color = White, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        color = LightGray.copy(alpha = 0.5f)
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it; viewModel.sendTyping(it.isNotEmpty()) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Mensaje", color = Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = PrimaryBlue,
                                focusedTextColor = Black,
                                unfocusedTextColor = Black
                            ),
                            maxLines = 4
                        )
                    }
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText.trim())
                                messageText = ""
                                viewModel.sendTyping(false)
                            }
                        },
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(
                            if (messageText.isNotBlank()) PrimaryBlue else LightGray
                        )
                    ) {
                        Icon(Icons.Default.Send, "Enviar", tint = White, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).background(LightGray.copy(alpha = 0.2f)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.messages) { msg ->
                if (msg.isMine) {
                    // Mensaje propio - derecha sin avatar sin nombre
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Surface(
                            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            color = LightBlue.copy(alpha = 0.5f),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(msg.content, style = MaterialTheme.typography.bodyLarge, color = Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    msg.createdAt.formatTime(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray,
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                } else {
                    // Mensaje recibido - avatar izquierda + nombre dentro de burbuja
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        // Avatar
                        Box(modifier = Modifier.size(28.dp)) {
                            if (msg.senderAvatar.isNotEmpty()) {
                                AsyncImage(
                                    model = msg.senderAvatar,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), color = LightBlue) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            msg.senderName.take(1).uppercase(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                            color = White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    msg.senderName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(msg.content, style = MaterialTheme.typography.bodyLarge, color = Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    msg.createdAt.formatTime(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray,
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
