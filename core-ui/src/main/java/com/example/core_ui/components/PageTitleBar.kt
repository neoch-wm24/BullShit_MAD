package com.example.core_ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core_resources.R
import androidx.compose.runtime.getValue

@Composable
private fun getTitleByRoute(route: String?): String {
    return when(route) {
        "home" -> stringResource(id = R.string.home_pt)
        "profile" -> stringResource(id = R.string.profile_pt)
        "setting" -> stringResource(id = R.string.setting_pt)
        "order" -> stringResource(id = R.string.order_and_parcel_pt)
        "delivery" -> stringResource(id = R.string.delivery_and_transportation_pt)
        "warehouse" -> stringResource(id = R.string.warehouse_pt)
        "user" -> stringResource(id = R.string.user_pt)
        "report" -> stringResource(id = R.string.report_pt)
        else -> "Unknown Page"
    }
}

@Composable
fun PageTitleBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // 获取标题
    val title = getTitleByRoute(currentRoute)

    // 判断是否需要显示返回按钮（主页面不显示）
    val showBackButton = currentRoute !in listOf("home", "profile", "setting")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawLine(
                    color = Color(0xFFDDDDDD),
                    start = Offset(0f, size.height - strokeWidth / 2),
                    end = Offset(size.width, size.height - strokeWidth / 2),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PageTitleBarPreview() {
    val navController = rememberNavController()
    PageTitleBar(navController = navController)
}