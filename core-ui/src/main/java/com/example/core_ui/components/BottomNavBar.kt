package com.example.core_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core_ui.theme.Black
import com.example.core_ui.theme.Grey


data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(navController: NavController, modifier: Modifier = Modifier) {
    val bottomNavItemList = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Profile", Icons.Default.Person, "profile"),
        BottomNavItem("Setting", Icons.Default.Settings, "setting")
    )

    NavigationBar(
        navController = navController,
        items = bottomNavItemList,
        modifier = modifier
    )
}

@Composable
fun NavigationBar(
    navController: NavController,
    items: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawLine(
                    color = Color(0xFFDDDDDD),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 自动状态管理 - 自动监听路由变化
            val currentDestination by navController.currentBackStackEntryAsState()

            items.forEach { item ->
                val isSelected = currentDestination?.destination?.route == item.route

                NavigationBarButton(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        // 自动导航逻辑 - 统一的导航处理
                        if (currentDestination?.destination?.route != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NavigationBarButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 预设样式和动画 - 颜色动画过渡
    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) Black else Grey,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_color_animation"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) Black else Grey,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "text_color_animation"
    )

    // 缩放动画效果
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.10f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .scale(animatedScale) // 应用缩放动画
            .clickable(
                onClick = onClick,
                // 使用 Material 3 的新 ripple API
                indication = ripple(
                    bounded = false,
                    radius = 30.dp,
                    color = Black
                ),
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            modifier = Modifier.size(26.dp),
            tint = animatedIconColor // 使用动画颜色
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            fontSize = 14.sp,
            color = animatedTextColor, // 使用动画颜色
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BottomNavBarPreview() {
    // 创建一个用于预览的 NavController
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
