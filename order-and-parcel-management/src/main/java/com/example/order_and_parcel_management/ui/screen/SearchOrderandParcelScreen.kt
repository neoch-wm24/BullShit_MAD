package com.example.order_and_parcel_management.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.components.BottomNavBar
import com.example.core_ui.components.PageTitleBar
import com.example.core_ui.theme.LogisticManagementApplicationTheme
import com.example.order_and_parcel_management.ui.components.FloatingActionButton

@Composable
fun SearchOrderAndParcelScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ){
        FloatingActionButton(
            navController = navController,
            modifier = Modifier
                .align(Alignment.BottomEnd)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrderAndParcelWithNavigationPreview() {
    LogisticManagementApplicationTheme {
        val navController = rememberNavController()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F8F8)),
            topBar = {
                PageTitleBar(navController = navController)
            },
            bottomBar = {
                BottomNavBar(navController = navController)
            }
        ) { innerPadding ->
            SearchOrderAndParcelScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                navController = navController,
            )
        }
    }
}