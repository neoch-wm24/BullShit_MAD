package com.example.core_ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.core_resources.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.theme.DefaultColor
import com.example.core_ui.theme.LogisticManagementApplicationTheme

@Composable
private fun getTitleByRoute(route: String?): String{
    return when(route) {
        "home" -> stringResource(id = R.string.home_pt)
        "order" -> stringResource(id = R.string.order_pt)
        "rak" -> stringResource(id = R.string.rak_pt)
        "profile" -> stringResource(id = R.string.profile_pt)
        "setting" -> stringResource(id = R.string.setting_pt)
        "other" -> stringResource(id = R.string.other_pt)
        else -> "Unknown Page"
    }
}
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val title = getTitleByRoute(currentRoute)
    val showBackButton = currentRoute !in listOf("home", "profile")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = modifier
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DefaultColor
                    )
                }
            }

            if (!showBackButton){
                Spacer(modifier = Modifier.size(16.dp))
            }

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DefaultColor
            )
        }
    }
}

@Preview(showBackground = false, showSystemUi = true)
@Composable
private fun TopBarFullScreenPreview() {
    val navController = rememberNavController()
    LogisticManagementApplicationTheme {
        Scaffold(
            topBar = {
                TopBar(navController = navController)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}