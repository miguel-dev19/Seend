package com.seend.app.ui.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seend.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onChatClick: (String, String) -> Unit,
    onNewChatClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ChatsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (uiState.connectionStatus) {
                                        ConnectionStatus.ESPERANDO_RED -> Color(0xFFFF9800)
                                        ConnectionStatus.CONECTANDO -> Color(0xFFFFEB3B)
                                        ConnectionStatus.ACTUALIZANDO -> Color(0xFF2196F3)
                                        ConnectionStatus.CONECTADO -> OnlineGreen
                                    }
                                )
                        )
                        Text(
                            text = uiState.connectionStatus.label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshChats() }) {
                        Icon(Icons.Default.Refresh, "Actualizar")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            Button(
                onClick = onNewChatClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.ChatBubble, contentDescription = "Nuevo chat", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Chat")
            }
        }
    ) { padding ->
        if (uiState.chats.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(80.dp), tint = Gray)
                    Text("No tienes conversaciones aún", style = MaterialTheme.typography.bodyLarge, color = Gray)
                    Text("Toca 'Nuevo Chat' para comenzar", style = MaterialTheme.typography.bodyMedium, color = Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)
            ) {
                items(uiState.chats) { chat ->
                    ChatItem(
                        chat = chat,
                        isTyping = uiState.typingUsers[chat.otherUser.id] == true,
                        onClick = { onChatClick(chat.id, chat.otherUser.username) }
                    )
                    Divider(
                        modifier = Modifier.padding(horizontal = 72.dp),
                        color = LightGray,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}
