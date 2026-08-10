package com.seend.app.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar con foto o iniciales
        Box(modifier = Modifier.size(56.dp)) {
            if (chat.otherUser.profilePic.isNotEmpty()) {
                AsyncImage(
                    model = chat.otherUser.profilePic,
                    contentDescription = "Foto",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(modifier = Modifier.fillMaxSize().clip(CircleShape), color = LightBlue) {
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
        
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(chat.otherUser.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Black)
            
            if (chat.lastMessage != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    when (chat.lastMsgStatus) {
                        MessageStatus.SENDING -> Icon(Icons.Default.Schedule, "Enviando", modifier = Modifier.size(14.dp), tint = Gray)
                        MessageStatus.SENT -> Icon(Icons.Default.Check, "Enviado", modifier = Modifier.size(14.dp), tint = Gray)
                        MessageStatus.DELIVERED -> Icon(Icons.Default.DoneAll, "Entregado", modifier = Modifier.size(14.dp), tint = Gray)
                        MessageStatus.READ -> Icon(Icons.Default.DoneAll, "Leído", modifier = Modifier.size(14.dp), tint = ReadBlue)
                        null -> {}
                    }
                    Text(chat.lastMessage, style = MaterialTheme.typography.bodyMedium, color = Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (chat.lastTime != null) Text(chat.lastTime.formatTime(), style = MaterialTheme.typography.bodySmall, color = Gray)
            if (chat.unreadCount > 0) {
                Surface(shape = CircleShape, color = PrimaryBlue, modifier = Modifier.size(22.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(chat.unreadCount.toString(), style = MaterialTheme.typography.labelMedium, color = White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
