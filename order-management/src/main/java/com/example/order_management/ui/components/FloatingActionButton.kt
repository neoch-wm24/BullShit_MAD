package com.example.order_management.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination

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
        ActionButtonItem(Icons.AutoMirrored.Filled.List, "Multiple Select Orders", "order_multiple_select"),
        ActionButtonItem(Icons.Default.Add, "Add", "AddOrder")
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
    val currentDestination by navController.currentBackStackEntryAsState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        ) {
            if (expanded) {
                items.forEach { item ->
                    val isSelected = currentDestination?.destination?.route == item.route

                    ActionButton(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (item.route == "order_multiple_select") {
                                onToggleMultiSelect?.invoke() // ✅ 多选模式回调
                            } else {
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
                        }
                    )
                }
            }

            // 主按钮
            ActionButton(
                item = ActionButtonItem(
                    icon = if (expanded) Icons.Default.Close else Icons.Default.Build,
                    contentDescription = "Toggle",
                    route = ""
                ),
                isSelected = false,
                onClick = { expanded = !expanded }
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