package com.seend.app.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seend.app.data.model.Chat
import com.seend.app.data.model.MessageStatus
import com.seend.app.ui.theme.*
import com.seend.app.util.formatTime

@Composable
fun ChatItem(
    chat: Chat,
    isTyping: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(modifier = Modifier.size(56.dp)) {
            if (chat.otherUser.profilePic.isNotEmpty()) {
                AsyncImage(
                    model = chat.otherUser.profilePic,
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
                    color = PrimaryBlue.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            chat.otherUser.username.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Nombre y mensaje
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = chat.otherUser.username,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = DarkGray
            )
            
            if (isTyping) {
                Text(
                    text = "escribiendo...",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnlineGreen,
                    fontWeight = FontWeight.Medium
                )
            } else if (chat.lastMessage != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Icono de estado del mensaje
                    when (chat.lastMsgStatus) {
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
                        null -> {}
                    }
                    
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // Hora y contador
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (chat.lastTime != null) {
                Text(
                    text = chat.lastTime.formatTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
            }
            
            if (chat.unreadCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
