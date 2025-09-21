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
    return when {
        route == "home" -> stringResource(id = R.string.home_pt)
        route == "report" -> stringResource(id = R.string.report_pt)
        route == "user" -> stringResource(id = R.string.user_pt)
        route == "AddUser" -> stringResource(id = R.string.add_user_pt)
        route?.startsWith("CustomerDetails") == true -> stringResource(id = R.string.customer_details_pt)
        route?.startsWith("Edit_User") == true -> stringResource(id = R.string.edit_user_pt)
        route == "order" -> stringResource(id = R.string.order_pt)
        route == "AddOrder" -> stringResource(id = R.string.add_order_pt)
        route?.startsWith("OrderDetails") == true -> stringResource(id = R.string.order_details_pt)
        route?.startsWith("Edit_Order") == true -> stringResource(id = R.string.edit_order_pt)
        route == "warehouse" -> stringResource(id = R.string.warehouse_pt)
        route == "AddRack" -> stringResource(id = R.string.add_rack_pt)
        route?.startsWith("RackDetails") == true -> stringResource(id = R.string.rack_details_pt)
        route == "delivery" -> stringResource(id = R.string.delivery_pt)
        route == "scan" -> stringResource(id = R.string.scan_pt)
        route == "profile" -> stringResource(id = R.string.profile_pt)
        route == "setting" -> stringResource(id = R.string.setting_pt)
        route == "other" -> stringResource(id = R.string.other_pt)
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