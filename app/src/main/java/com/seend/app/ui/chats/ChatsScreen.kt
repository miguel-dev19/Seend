package com.seend.app.ui.chats

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seend.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onChatClick: (String, String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ChatsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        AnimatedContent(
                            targetState = uiState.connectionStatus,
                            transitionSpec = {
                                fadeIn() + slideInHorizontally() togetherWith
                                fadeOut() + slideOutHorizontally()
                            }
                        ) { status ->
                            Text(
                                text = status,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Black,
                                fontSize = 22.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
                )
                Divider(color = LightGray, thickness = 0.5.dp)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(White)) {
            if (uiState.chats.isEmpty() && !uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Outlined.ChatBubbleOutline, null, modifier = Modifier.size(80.dp), tint = LightGray)
                        Text("No tienes conversaciones aún", style = MaterialTheme.typography.bodyLarge, color = Gray)
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.chats) { chat ->
                        ChatItem(chat = chat, onClick = { onChatClick(chat.id, chat.otherUser.username) })
                        Divider(color = LightGray, thickness = 0.5.dp)
                    }
                }
            }
            
            // Botón "Nuevo Chat" inline - sin fondo flotante
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { onNewChatClick() },
                color = White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        "Nuevo chat",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Nuevo Chat",
                        style = MaterialTheme.typography.titleMedium,
                        color = Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Divider(color = LightGray, thickness = 0.5.dp)
        }
    }
}
