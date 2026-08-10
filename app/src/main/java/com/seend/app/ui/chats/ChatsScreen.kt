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
            TopAppBar(
                title = {
                    Text(
                        text = uiState.connectionStatus,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Black,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        floatingActionButton = {
            Button(
                onClick = onNewChatClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.padding(16.dp).height(56.dp)
            ) {
                Icon(Icons.Default.ChatBubble, "Nuevo chat", modifier = Modifier.size(22.dp), tint = White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Nuevo Chat", color = White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    ) { padding ->
        if (uiState.chats.isEmpty() && !uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(White), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(80.dp), tint = LightGray)
                    Text("No tienes conversaciones aún", style = MaterialTheme.typography.bodyLarge, color = Gray)
                    Text("Toca 'Nuevo Chat' para comenzar", style = MaterialTheme.typography.bodyMedium, color = Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(White)) {
                items(uiState.chats) { chat ->
                    ChatItem(chat = chat, onClick = { onChatClick(chat.id, chat.otherUser.username) })
                    Divider(modifier = Modifier.padding(horizontal = 72.dp), color = LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}
