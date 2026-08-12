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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seend.app.ui.theme.*
import com.seend.app.util.formatLastSeen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onBackClick: () -> Unit,
    onUserClick: (String, String) -> Unit,
    viewModel: UsersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedUsername by remember { mutableStateOf("") }

    LaunchedEffect(uiState.createdChatId) {
        uiState.createdChatId?.let { chatId ->
            if (chatId.isNotEmpty()) {
                viewModel.clearCreatedChat()
                onUserClick(chatId, selectedUsername)
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Nuevo Chat", fontWeight = FontWeight.Bold, color = Black) },
                    navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Volver", tint = Black) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
                )
                Divider(color = LightGray, thickness = 0.5.dp)
            }
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
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedUsername = user.username
                            viewModel.createChat(user)
                        }.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(56.dp)) {
                            if (user.profilePic.isNotEmpty()) {
                                AsyncImage(model = user.profilePic, contentDescription = "Foto", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                            } else {
                                Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), color = LightBlue) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(user.username.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Black)
                            Text(
                                text = when {
                                    user.isOnline -> "En línea"
                                    user.lastSeen != null -> user.lastSeen!!.formatLastSeen()
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (user.isOnline) OnlineGreen else Gray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Divider(color = LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}
