package com.seend.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seend.app.di.AppModule
import com.seend.app.ui.auth.*
import com.seend.app.ui.chats.*
import com.seend.app.ui.theme.*
import com.seend.app.ui.users.*

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CHATS = "chats"
    const val USERS = "users"
    const val CHAT_DETAIL = "chat_detail/{chatId}/{username}"
    
    fun chatDetail(chatId: String, username: String) = "chat_detail/$chatId/$username"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    val authRepository = remember { AppModule.provideAuthRepository() }
    val webSocketManager = remember { AppModule.provideWebSocketManager() }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, webSocketManager)
    )
    
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    val startDestination = if (isLoggedIn) Routes.CHATS else Routes.WELCOME
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onContinueClick = { navController.navigate(Routes.LOGIN) }
            )
        }
        
        composable(Routes.LOGIN) {
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val loginSuccess by authViewModel.loginSuccess.collectAsState()
            
            LaunchedEffect(loginSuccess) {
                if (loginSuccess) {
                    navController.navigate(Routes.CHATS) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            
            LoginScreen(
                onLoginClick = { username, password ->
                    authViewModel.login(username, password)
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
        
        composable(Routes.REGISTER) {
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val loginSuccess by authViewModel.loginSuccess.collectAsState()
            
            LaunchedEffect(loginSuccess) {
                if (loginSuccess) {
                    navController.navigate(Routes.CHATS) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            
            RegisterScreen(
                onRegisterClick = { name, username, password, photoUri ->
                    authViewModel.register(name, username, password, photoUri)
                },
                onBackClick = { navController.popBackStack() },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
        
        composable(Routes.CHATS) {
            val chatRepository = remember { AppModule.provideChatRepository() }
            val chatsViewModel: ChatsViewModel = viewModel(
                factory = ChatsViewModelFactory(chatRepository, webSocketManager)
            )
            
            ChatsScreen(
                onChatClick = { chatId, username ->
                    navController.navigate(Routes.chatDetail(chatId, username))
                },
                onNewChatClick = {
                    navController.navigate(Routes.USERS)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = chatsViewModel
            )
        }
        
        composable(Routes.USERS) {
            val userRepository = remember { AppModule.provideUserRepository() }
            val chatRepository = remember { AppModule.provideChatRepository() }
            val usersViewModel: UsersViewModel = viewModel(
                factory = UsersViewModelFactory(userRepository, chatRepository)
            )
            
            UsersScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { chatId ->
                    navController.navigate(Routes.chatDetail(chatId, "")) {
                        popUpTo(Routes.CHATS)
                    }
                },
                viewModel = usersViewModel
            )
        }
        
        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            
            ChatDetailPlaceholder(
                chatId = chatId,
                username = username,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailPlaceholder(
    chatId: String,
    username: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(username) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Chat con $username\n(próximamente)",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray
            )
        }
    }
}
