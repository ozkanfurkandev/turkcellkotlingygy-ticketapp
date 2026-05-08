package com.turkcell.ticketapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.turkcell.core.ui.theme.TicketAppTheme
import com.turkcell.ticketapp.presentation.auth.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TicketAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    LoginScreen(
                        onLoginSuccess = { session ->
                            Toast.makeText(
                                context,
                                "Hoş geldin ${session.user.email}",
                                Toast.LENGTH_LONG,
                            ).show()
                        },
                    )
                }
            }
        }
    }
}
