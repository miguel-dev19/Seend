package com.seend.app.ui.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seend.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(onBackClick: () -> Unit, onUserClick: (String) -> Unit, viewModel: UsersViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.createdChatId) {
        uiState.createdChatId?.let { chatId ->
            viewModel.clearCreatedChat()
            onUserClick(chatId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Chat", fontWeight = FontWeight.Bold, color = Black) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Volver", tint = Black) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (uiState.users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(White), contentAlignment = Alignment.Center) {
                Text("No se encontraron usuarios", style = MaterialTheme.typography.bodyLarge, color = Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(White)) {
                items(uiState.users) { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.createChat(user.id) }.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(modifier = Modifier.size(56.dp).clip(CircleShape), color = LightBlue) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(user.username.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Black)
                            Text(user.info, style = MaterialTheme.typography.bodySmall, color = Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        // Sin flecha >
                    }
                    Divider(modifier = Modifier.padding(horizontal = 72.dp), color = LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}
