package com.seend.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seend.app.di.AppModule
import com.seend.app.ui.auth.*
import com.seend.app.ui.chats.*
import com.seend.app.ui.profile.*
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
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val authRepository = remember { AppModule.provideAuthRepository() }
    val webSocketManager = remember { AppModule.provideWebSocketManager() }
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository, webSocketManager))
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val startDestination = if (isLoggedIn) Routes.CHATS else Routes.WELCOME

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onContinueClick = { navController.navigate(Routes.LOGIN) })
        }
        composable(Routes.LOGIN) {
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val loginSuccess by authViewModel.loginSuccess.collectAsState()
            LaunchedEffect(loginSuccess) { if (loginSuccess) navController.navigate(Routes.CHATS) { popUpTo(0) { inclusive = true } } }
            LoginScreen(onLoginClick = { u, p -> authViewModel.login(u, p) }, onRegisterClick = { navController.navigate(Routes.REGISTER) }, isLoading = isLoading, errorMessage = errorMessage)
        }
        composable(Routes.REGISTER) {
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val loginSuccess by authViewModel.loginSuccess.collectAsState()
            LaunchedEffect(loginSuccess) { if (loginSuccess) navController.navigate(Routes.CHATS) { popUpTo(0) { inclusive = true } } }
            RegisterScreen(onRegisterClick = { n, u, p, photo -> authViewModel.register(n, u, p, photo) }, onBackClick = { navController.popBackStack() }, isLoading = isLoading, errorMessage = errorMessage)
        }
        composable(Routes.CHATS) {
            val chatRepository = remember { AppModule.provideChatRepository() }
            val chatsViewModel: ChatsViewModel = viewModel(factory = ChatsViewModelFactory(application, chatRepository, webSocketManager))
            ChatsScreen(
                onChatClick = { chatId, username ->
                    if (chatId.isNotEmpty()) navController.navigate(Routes.chatDetail(chatId, username))
                },
                onNewChatClick = { navController.navigate(Routes.USERS) },
                viewModel = chatsViewModel
            )
        }
        composable(Routes.USERS) {
            val userRepository = remember { AppModule.provideUserRepository() }
            val chatRepository = remember { AppModule.provideChatRepository() }
            val usersViewModel: UsersViewModel = viewModel(factory = UsersViewModelFactory(userRepository, chatRepository))
            UsersScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { chatId ->
                    if (chatId.isNotEmpty()) {
                        navController.navigate(Routes.chatDetail(chatId, "")) {
                            popUpTo(Routes.CHATS)
                        }
                    }
                },
                viewModel = usersViewModel
            )
        }
        composable(route = Routes.CHAT_DETAIL, arguments = listOf(navArgument("chatId") { type = NavType.StringType }, navArgument("username") { type = NavType.StringType })) { entry ->
            val chatId = entry.arguments?.getString("chatId") ?: ""
            val username = entry.arguments?.getString("username") ?: ""
            
            if (chatId.isEmpty()) {
                // Si chatId es vacío, volver a chats
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                val chatRepository = remember { AppModule.provideChatRepository() }
                val userRepository = remember { AppModule.provideUserRepository() }
                val vm: ChatDetailViewModel = viewModel(factory = ChatDetailViewModelFactory(chatId, chatRepository, userRepository, webSocketManager))
                ChatDetailScreen(chatId = chatId, username = username, onBackClick = { navController.popBackStack() }, onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) }, viewModel = vm)
            }
        }
        composable(route = Routes.PROFILE, arguments = listOf(navArgument("userId") { type = NavType.StringType })) { entry ->
            val userId = entry.arguments?.getString("userId") ?: ""
            val userRepository = remember { AppModule.provideUserRepository() }
            val vm: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(userId, userRepository))
            val user by vm.user.collectAsState()
            val isLoading by vm.isLoading.collectAsState()
            ProfileScreen(user = user, isLoading = isLoading, onBackClick = { navController.popBackStack() })
        }
    }
}
