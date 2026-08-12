package com.seend.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seend.app.di.AppModule
import com.seend.app.ui.auth.*
import com.seend.app.ui.chats.*

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val GLOBAL_CHAT = "global_chat"
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
    val startDestination = if (isLoggedIn) Routes.GLOBAL_CHAT else Routes.WELCOME

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(onContinueClick = { navController.navigate(Routes.LOGIN) })
        }
        composable(Routes.LOGIN) {
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val loginSuccess by authViewModel.loginSuccess.collectAsState()
            LaunchedEffect(loginSuccess) { 
                if (loginSuccess) navController.navigate(Routes.GLOBAL_CHAT) { popUpTo(0) { inclusive = true } }
            }
            LoginScreen(
                onLoginClick = { u, p -> authViewModel.login(u, p) },
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
                if (loginSuccess) navController.navigate(Routes.GLOBAL_CHAT) { popUpTo(0) { inclusive = true } }
            }
            RegisterScreen(
                onRegisterClick = { n, u, p, photo -> authViewModel.register(n, u, p, photo) },
                onBackClick = { navController.popBackStack() },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
        composable(Routes.GLOBAL_CHAT) {
            val chatRepository = remember { AppModule.provideChatRepository() }
            val userRepository = remember { AppModule.provideUserRepository() }
            val globalChatVM: GlobalChatViewModel = viewModel(
                factory = GlobalChatViewModelFactory(application, chatRepository, userRepository, webSocketManager)
            )
            GlobalChatScreen(viewModel = globalChatVM)
        }
    }
}
