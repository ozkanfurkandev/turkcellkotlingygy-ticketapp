package com.turkcell.ticketapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.ticketapp.navigation.Home
import com.turkcell.ticketapp.navigation.TicketDetail
import com.turkcell.ticketapp.screen.HomeScreen
import com.turkcell.ticketapp.screen.LoginScreen
import com.turkcell.ticketapp.screen.RegisterScreen
import com.turkcell.ticketapp.screen.TicketDetailScreen
import org.koin.compose.koinInject


@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject()
)
{
    val isLoggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

    when(isLoggedIn)
    {
        null -> SplashScreen()
        true -> AuthedNavHost(navController)
        false -> UnAuthedNavHost(navController)
    }
}

@Composable
private fun SplashScreen(){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        CircularProgressIndicator()
    }
}

@Composable
private fun AuthedNavHost(navController: NavHostController){
    NavHost(navController=navController, startDestination = Home){
        composable<Home> {
            HomeScreen(
                onTicketClick = { ticketId ->
                    navController.navigate(TicketDetail(ticketId = ticketId))
                },
            )
        }
        composable<TicketDetail> {
            TicketDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun UnAuthedNavHost(navController: NavHostController){
    NavHost(navController=navController, startDestination = Login) {
        composable<Login>{
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {navController.navigate(Register)}
            )
        }
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {},
                onNavigateToLogin = {
                    if (!navController.popBackStack(Login, inclusive = false)) {
                        navController.navigate(Login) {
                            popUpTo(Register) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}