package com.example.ims_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ims_app.navigation.ImsAppNav
import com.example.ims_app.ui.theme.IMS_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IMS_AppTheme {
                ImsAppNav()
            }
        }
    }
}