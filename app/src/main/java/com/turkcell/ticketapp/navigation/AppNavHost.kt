package com.turkcell.ticketapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.auth.UserRole
import com.turkcell.ticketapp.screen.EventDetailScreen
import com.turkcell.ticketapp.screen.HomeScreen
import com.turkcell.ticketapp.screen.LoginScreen
import com.turkcell.ticketapp.screen.MyPurchasesScreen
import com.turkcell.ticketapp.screen.RegisterScreen
import com.turkcell.ticketapp.screen.StaffScreen
import com.turkcell.ticketapp.screen.TicketDetailScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = koinInject(),
) {
    val isLoggedIn by authRepository.isLoggedIn.collectAsStateWithLifecycle(initialValue = null)

    when (isLoggedIn) {
        null -> SplashScreen()
        true -> AuthedNavHost(navController, authRepository)
        false -> UnAuthedNavHost(navController)
    }
}

@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AuthedNavHost(
    navController: NavHostController,
    authRepository: AuthRepository,
) {
    val userRole by authRepository.userRole.collectAsStateWithLifecycle(initialValue = null)

    if (userRole == null) {
        SplashScreen()
        return
    }

    when (userRole!!) {
        UserRole.STAFF -> StaffNavHost(navController)
        UserRole.USER -> CustomerNavHost(navController, showStaffEntry = false)
        UserRole.ADMIN -> CustomerNavHost(navController, showStaffEntry = true)
    }
}

@Composable
private fun StaffNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Staff) {
        composable<Staff> {
            StaffScreen()
        }
    }
}

@Composable
private fun CustomerNavHost(
    navController: NavHostController,
    showStaffEntry: Boolean,
) {
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> { entry ->
            val openTicketsTab by entry.savedStateHandle
                .getStateFlow("openTicketsTab", false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(openTicketsTab) {
                if (openTicketsTab) {
                    entry.savedStateHandle["openTicketsTab"] = false
                }
            }

            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(EventDetail(id = eventId))
                },
                onTicketClick = { ticketId ->
                    navController.navigate(TicketDetail(ticketId = ticketId))
                },
                onNavigateToPurchases = { navController.navigate(MyPurchases) },
                onNavigateToStaff = { navController.navigate(Staff) },
                showStaffEntry = showStaffEntry,
                openTicketsTab = openTicketsTab,
            )
        }
        composable<EventDetail> {
            EventDetailScreen(
                onBack = { navController.popBackStack() },
                onPurchaseSuccess = {
                    navController.getBackStackEntry(Home).savedStateHandle["openTicketsTab"] = true
                    navController.popBackStack(Home, inclusive = false)
                },
            )
        }
        composable<TicketDetail> {
            TicketDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<MyPurchases> {
            MyPurchasesScreen(onBack = { navController.popBackStack() })
        }
        composable<Staff> {
            StaffScreen()
        }
    }
}

@Composable
private fun UnAuthedNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = { navController.navigate(Register) },
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
                },
            )
        }
    }
}
