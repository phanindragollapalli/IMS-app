package com.example.ims_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ims_app.data.DemoRepository
import com.example.ims_app.data.SessionManager
import com.example.ims_app.navigation.ImsAppNav
import com.example.ims_app.screens.LoginScreen
import com.example.ims_app.ui.theme.IMS_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IMS_AppTheme {
                val sessionManager = remember { SessionManager(applicationContext) }
                var currentUser by remember { mutableStateOf(sessionManager.currentSessionUser()) }
                DemoRepository.updateCurrentUser(currentUser)

                if (currentUser != null) {
                    ImsAppNav(
                        onLogout = {
                            sessionManager.logout()
                            currentUser = null
                            DemoRepository.updateCurrentUser(null)
                        }
                    )
                } else {
                    LoginScreen(
                        onLogin = { username, password ->
                            val success = sessionManager.login(username, password)
                            if (success) {
                                currentUser = sessionManager.currentSessionUser()
                                DemoRepository.updateCurrentUser(currentUser)
                                true
                            } else {
                                false
                            }
                        }
                    )
                }
            }
        }
    }
}
