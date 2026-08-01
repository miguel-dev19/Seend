package com.seend.app.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seend.app.di.AppModule
import com.seend.app.ui.auth.*
import com.seend.app.ui.chats.*
import com.seend.app.ui.users.*

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CHATS = "chats"
    const val USERS = "users"
    const val CHAT_DETAIL = "chat_detail/{chatId}/{username}"
    const val PROFILE = "profile/{userId}"
    
    fun chatDetail(chatId: String, username: String) = "chat_detail/$chatId/$username"
    fun profile(userId: String) = "profile/$userId"
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
            
            val chatRepository = remember { AppModule.provideChatRepository() }
            val userRepository = remember { AppModule.provideUserRepository() }
            
            val chatDetailViewModel: ChatDetailViewModel = viewModel(
                factory = ChatDetailViewModelFactory(
                    chatId = chatId,
                    chatRepository = chatRepository,
                    userRepository = userRepository,
                    webSocketManager = webSocketManager
                )
            )
            
            ChatDetailScreen(
                chatId = chatId,
                username = username,
                onBackClick = { navController.popBackStack() },
                onProfileClick = { userId ->
                    navController.navigate(Routes.profile(userId))
                },
                viewModel = chatDetailViewModel
            )
        }
        
        composable(
            route = Routes.PROFILE,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            // Placeholder para perfil (próximamente)
            androidx.compose.material3.Scaffold(
                topBar = {
                    androidx.compose.material3.ExperimentalMaterial3Api::class
                    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
                    androidx.compose.material3.TopAppBar(
                        title = { androidx.compose.material3.Text("Perfil") },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                                androidx.compose.material3.Icon(
                                    androidx.compose.material.icons.Icons.Default.ArrowBack,
                                    "Volver"
                                )
                            }
                        },
                        colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = com.seend.app.ui.theme.PrimaryBlue,
                            titleContentColor = androidx.compose.ui.graphics.Color.White,
                            navigationIconContentColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            ) { padding ->
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        "Perfil de usuario\n(próximamente)",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = com.seend.app.ui.theme.Gray
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.Modifier.fillMaxSize() = this.then(
    androidx.compose.foundation.layout.fillMaxSize()
)

private fun androidx.compose.ui.Modifier.padding(padding: androidx.compose.foundation.layout.PaddingValues) = this.then(
    androidx.compose.foundation.layout.padding(padding)
)
