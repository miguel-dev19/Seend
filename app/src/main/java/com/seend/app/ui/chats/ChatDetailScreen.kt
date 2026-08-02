package com.seend.app.ui.chats

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
import com.seend.app.util.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    username: String,
    onBackClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: ChatDetailViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.clickable {
                            uiState.otherUser?.let { onProfileClick(it.id) }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            if (uiState.otherUser?.profilePic?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = uiState.otherUser!!.profilePic,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    color = Color.White.copy(alpha = 0.3f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            username.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = username,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when {
                                    uiState.isTyping -> "Escribiendo..."
                                    uiState.otherUser?.isOnline == true -> "En línea"
                                    uiState.otherUser?.lastSeen != null -> 
                                        "Últ. vez ${uiState.otherUser!!.lastSeen!!.formatTime()}"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = {
                            messageText = it
                            viewModel.sendTyping(it.isNotEmpty())
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...", color = Gray) },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = LightGray,
                            focusedContainerColor = LightGray.copy(alpha = 0.3f),
                            unfocusedContainerColor = LightGray.copy(alpha = 0.3f)
                        ),
                        maxLines = 4,
                        singleLine = false
                    )
                    
                    if (messageText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val receiverId = uiState.otherUser?.id ?: return@IconButton
                                viewModel.sendMessage(messageText.trim(), receiverId)
                                messageText = ""
                                viewModel.sendTyping(false)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                        ) {
                            Icon(
                                Icons.Default.Send,
                                "Enviar",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(
                    message = message,
                    isMine = message.senderId != uiState.otherUser?.id
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isMine: Boolean
) {
    val bubbleColor = if (isMine) Color.White else PrimaryBlue
    val textColor = if (isMine) Color.Black else Color.White
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (message.createdAt.isNotEmpty()) message.createdAt.formatTime() else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isMine) Gray else Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    
                    if (isMine) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.SENDING -> Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Enviando",
                                modifier = Modifier.size(14.dp),
                                tint = Gray
                            )
                            MessageStatus.SENT -> Icon(
                                Icons.Default.Check,
                                contentDescription = "Enviado",
                                modifier = Modifier.size(14.dp),
                                tint = Gray
                            )
                            MessageStatus.DELIVERED -> Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Entregado",
                                modifier = Modifier.size(14.dp),
                                tint = Gray
                            )
                            MessageStatus.READ -> Icon(
                                Icons.Default.DoneAll,
                                contentDescription = "Leído",
                                modifier = Modifier.size(14.dp),
                                tint = ReadBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
