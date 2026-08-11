package com.seend.app.ui.chats

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.seend.app.data.model.Message
import com.seend.app.data.model.MessageStatus
import com.seend.app.ui.theme.*
import com.seend.app.util.formatLastSeen
import com.seend.app.util.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String, username: String,
    onBackClick: () -> Unit, onProfileClick: (String) -> Unit,
    viewModel: ChatDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.size - 1)
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Volver", tint = Black) } },
                    title = {
                        Row(
                            modifier = Modifier.clickable { uiState.otherUser?.let { onProfileClick(it.id) } },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(40.dp)) {
                                if (uiState.otherUser?.profilePic?.isNotEmpty() == true) {
                                    AsyncImage(
                                        model = uiState.otherUser!!.profilePic,
                                        contentDescription = "Foto",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), color = LightBlue) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(username.take(1).uppercase(), color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(username, style = MaterialTheme.typography.titleMedium, color = Black, fontWeight = FontWeight.Bold)
                                AnimatedContent(
                                    targetState = when {
                                        uiState.connectionStatus != "Conectado" -> uiState.connectionStatus
                                        uiState.isTyping -> "Escribiendo..."
                                        uiState.otherUser?.isOnline == true -> "En línea"
                                        uiState.otherUser?.lastSeen != null -> uiState.otherUser!!.lastSeen!!.formatLastSeen()
                                        else -> ""
                                    },
                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                                ) { status ->
                                    Text(status, style = MaterialTheme.typography.bodySmall, color = Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
                )
                Divider(color = LightGray, thickness = 0.5.dp)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Lista de mensajes
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().background(LightGray.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                }
            }

            // Barra de escritura flotante estilo Telegram/WhatsApp
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                color = White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Input redondeado
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
                                val receiverId = uiState.otherUser?.id ?: return@IconButton
                                viewModel.sendMessage(messageText.trim(), receiverId)
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
                    // Botón enviar circular (como Telegram/WhatsApp)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    val bubbleColor = if (isMine) LightBlue.copy(alpha = 0.5f) else White
    val textColor = Black
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(message.content, style = MaterialTheme.typography.bodyLarge, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (message.createdAt.isNotEmpty()) message.createdAt.formatTime() else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray,
                        fontSize = 10.sp
                    )
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.SENDING -> Icon(Icons.Outlined.Schedule, "Enviando", modifier = Modifier.size(14.dp), tint = Gray)
                            MessageStatus.SENT -> Icon(Icons.Outlined.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Gray)
                            MessageStatus.DELIVERED -> Icon(Icons.Outlined.DoneAll, "Entregado", modifier = Modifier.size(14.dp), tint = Gray)
                            MessageStatus.READ -> Icon(Icons.Outlined.DoneAll, "Leído", modifier = Modifier.size(14.dp), tint = ReadBlue)
                        }
                    }
                }
            }
        }
    }
}
