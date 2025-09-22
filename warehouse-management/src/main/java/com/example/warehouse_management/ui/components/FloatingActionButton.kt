package com.example.warehouse_management.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// 数据类 - 定义操作按钮项
data class ActionButtonItem(
    val icon: ImageVector,
    val contentDescription: String,
    val route: String
)

// 主要浮动操作按钮组件
@Composable
fun FloatingActionButton(
    navController: NavController,
    modifier: Modifier = Modifier,
    onToggleMultiSelect: (() -> Unit)? = null
) {
    val actionButtonItemList = listOf(
        ActionButtonItem(Icons.AutoMirrored.Filled.List, "Multiple Select", "multiple_select"),
        ActionButtonItem(Icons.Default.Add, "Add", "AddRack")
    )

    NavigationActionButton(
        navController = navController,
        items = actionButtonItemList,
        modifier = modifier,
        onToggleMultiSelect = onToggleMultiSelect
    )
}

// 导航操作按钮容器组件
@Composable
fun NavigationActionButton(
    navController: NavController,
    items: List<ActionButtonItem>,
    modifier: Modifier = Modifier,
    onToggleMultiSelect: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        ) {
            // 自动状态管理 - 自动监听路由变化
            val currentDestination by navController.currentBackStackEntryAsState()

            if (expanded) {
                items.forEach { item ->
                    val isSelected = currentDestination?.destination?.route == item.route

                    ActionButton(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (item.route == "multiple_select") {
                                onToggleMultiSelect?.invoke()
                            } else {
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
                            }
                            expanded = false
                        },
                        modifier = Modifier
                    )
                }
            }

            ActionButton(
                item = ActionButtonItem(
                    icon = if (expanded) Icons.Default.Close else Icons.Default.Build,
                    contentDescription = "Toggle",
                    route = ""
                ),
                isSelected = false,
                onClick = { expanded = !expanded },
                modifier = Modifier
            )
        }
    }
}

// 单个操作按钮组件
@Composable
fun ActionButton(
    item: ActionButtonItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 预设样式和动画 - 颜色动画过渡
    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_color_animation"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "background_color_animation"
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

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(animatedScale) // 应用缩放动画
            .clip(CircleShape)
            .background(animatedBackgroundColor)
            .border(2.dp, Color.Black, CircleShape)
            .clickable(
                onClick = onClick,
                // 使用 Material 3 的新 ripple API
                indication = ripple(
                    bounded = false,
                    radius = 28.dp,
                    color = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = animatedIconColor, // 使用动画颜色
            modifier = Modifier.size(28.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RackInformation_Preview() {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(navController = navController)
    }
}