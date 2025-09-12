package com.example.delivery_and_transportation_management.ui.components

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
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

// Constants
private val ButtonSize = 56.dp
private val ButtonPadding = 24.dp
private val IconSize = 28.dp

data class ActionButtonItem(
    val icon: ImageVector,
    val contentDescription: String,
    val route: String
)

@Composable
fun ActionButtonMenu(
    navController: NavController,
    modifier: Modifier = Modifier,
    onToggleMultiSelect: (() -> Unit)? = null
) {
    val actionButtonItemList = listOf(
        ActionButtonItem(Icons.AutoMirrored.Filled.List, "Multiple Select", "multiple_select"),
        ActionButtonItem(Icons.Default.Add, "Add", "add_transportation")
    )

    NavigationActionButton(
        navController = navController,
        items = actionButtonItemList,
        modifier = modifier,
        onToggleMultiSelect = onToggleMultiSelect
    )
}

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
                .padding(end = ButtonPadding, bottom = ButtonPadding)
        ) {
            if (expanded) {
                items.forEach { item ->
                    val isSelected = currentDestination?.destination?.route == item.route
                    ActionButton(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (item.route == "multiple_select") {
                                // 切换多选模式
                                onToggleMultiSelect?.invoke()
                            } else {
                                // 导航到 Add 页面
                                navigateToRoute(
                                    navController,
                                    item.route,
                                    currentDestination?.destination?.route
                                )
                            }
                            expanded = false
                        }
                    )
                }
            }

            // 主按钮（展开/收起）
            ActionButton(
                item = ActionButtonItem(
                    icon = if (expanded) Icons.Default.Close else Icons.Default.Build,
                    contentDescription = "Toggle Action Buttons",
                    route = ""
                ),
                isSelected = false,
                onClick = { expanded = !expanded }
            )
        }
    }
}

private fun navigateToRoute(navController: NavController, route: String, currentRoute: String?) {
    if (route.isNotEmpty() && currentRoute != route) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun ActionButton(
    item: ActionButtonItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.Black,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_color_animation"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "background_color_animation"
    )

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
            .size(ButtonSize)
            .scale(animatedScale)
            .clip(CircleShape)
            .background(animatedBackgroundColor)
            .border(2.dp, Color.Black, CircleShape)
            .clickable(
                onClick = onClick,
                indication = ripple(
                    bounded = false,
                    radius = ButtonSize / 2,
                    color = Color.Black
                ),
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDescription,
            tint = animatedIconColor,
            modifier = Modifier.size(IconSize)
        )
    }
}
