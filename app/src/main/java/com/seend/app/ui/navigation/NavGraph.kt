package com.seend.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seend.app.data.api.WebSocketManager
import com.seend.app.data.repository.AuthRepository
import com.seend.app.data.repository.ChatRepository
import com.seend.app.data.repository.UserRepository
import com.seend.app.di.AppModule
import com.seend.app.ui.auth.*
import com.seend.app.ui.chats.ChatsScreen
import com.seend.app.util.TokenManager

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CHATS = "chats"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val tokenManager = remember { TokenManager }
    
    val authApi = remember { AppModule.provideAuthApi(tokenManager) }
    val seendApi = remember { AppModule.provideSeendApi(tokenManager) }
    val authRepository = remember { AppModule.provideAuthRepository(authApi) }
    val webSocketManager = remember { AppModule.provideWebSocketManager() }
    
    val authViewModel: AuthViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authRepository, webSocketManager) as T
            }
        }
    )
    
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    val startDestination = if (isLoggedIn) Routes.CHATS else Routes.WELCOME
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onContinueClick = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }
        
        composable(Routes.LOGIN) {
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val loginSuccess by authViewModel.loginSuccess.collectAsState()
            
            if (loginSuccess) {
                navController.navigate(Routes.CHATS) {
                    popUpTo(0) { inclusive = true }
                }
            }
            
            LoginScreen(
                onLoginClick = { username, password ->
                    authViewModel.login(username, password)
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
        
        composable(Routes.REGISTER) {
            val isLoading by authViewModel.isLoading.collectAsState()
            val errorMessage by authViewModel.errorMessage.collectAsState()
            val loginSuccess by authViewModel.loginSuccess.collectAsState()
            
            if (loginSuccess) {
                navController.navigate(Routes.CHATS) {
                    popUpTo(0) { inclusive = true }
                }
            }
            
            RegisterScreen(
                onRegisterClick = { name, username, password, photoUri ->
                    authViewModel.register(name, username, password, photoUri)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                isLoading = isLoading,
                errorMessage = errorMessage
            )
        }
        
        composable(Routes.CHATS) {
            ChatsScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
