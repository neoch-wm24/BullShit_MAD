package com.example.main_screen.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.main_screen.viewmodel.AuthViewModel


@Composable
fun ProfilePage (
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel
){
    Text(text = "Profile Page is on the way")

    ProfileContent(
        modifier = modifier,
        navController
    )
}

@Composable
fun ProfileContent(modifier: Modifier = Modifier, navController: NavController) {
    Column {
    }
}