package com.example.logistic_management_application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.logistic_management_application.ui.theme.LogisticManagementApplicationTheme
import com.example.main_screen.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            LogisticManagementApplicationTheme {
                MyAppNavigation(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel
                )
            }
        }
    }
}