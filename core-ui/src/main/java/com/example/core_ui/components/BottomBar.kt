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
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.core_ui.theme.ButtonAnimationColor
import com.example.core_ui.theme.ButtonUnselectedColor
import com.example.core_ui.theme.LogisticManagementApplicationTheme
import com.example.core_ui.theme.White

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val isSpecial: Boolean = false
)

@Composable
fun BottomBar(navController: NavController, modifier: Modifier = Modifier){
    val navigationItemList = listOf(
        NavigationItem("Home", Icons.Default.Home, "home"),
        NavigationItem("Scan", Icons.Default.QrCodeScanner, "scan", isSpecial = true),
        NavigationItem("Profile", Icons.Default.Person, "profile"),
    )

    NavigationBar(
        navController = navController,
        items = navigationItemList,
        modifier = modifier
    )
}

@Composable
fun NavigationBar(
    navController: NavController,
    items: List<NavigationItem>,
    modifier: Modifier = Modifier
){
    val specialItem = items.find { it.isSpecial }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Surface(
            shadowElevation = 10.dp,
            shape = RoundedCornerShape(12.dp)
        ) {

            Row(
                modifier = modifier
                    .height(65.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentDestination by navController.currentBackStackEntryAsState()

                items.forEach { item ->
                    val isSelected = currentDestination?.destination?.route == item.route
                    if (!item.isSpecial){
                        NavigationButton(
                            item = item,
                            isSelected = isSelected,
                            onClick = {
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
                    }else{
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        specialItem?.let { item ->
            val currentDestination by navController.currentBackStackEntryAsState()
            val isSelected = currentDestination?.destination?.route == item.route

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                NavigationButton(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
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
                )
            }
        }
    }
}

@Composable
fun NavigationButton(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetColor = when {
        isPressed -> ButtonUnselectedColor
        isSelected -> MaterialTheme.colorScheme.primary
        else -> ButtonUnselectedColor
    }

    val animatedIconColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_color_animation"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "text_color_animation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.10f else if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )

    Box(
        modifier = modifier
            .offset(y = if (item.isSpecial) (-20).dp else 0.dp)
            .scale(animatedScale)
            .clickable(
                onClick = onClick,
                indication = ripple(
                    bounded = false,
                    radius = if (item.isSpecial) 60.dp else 30.dp,
                    color = ButtonAnimationColor
                ),
                interactionSource = interactionSource
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .size(65.dp)
                .clip(CircleShape)
                .background(if (item.isSpecial) MaterialTheme.colorScheme.primary else Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(if (item.isSpecial) 32.dp else 26.dp),
                tint = if (item.isSpecial) White else animatedIconColor
            )

            if (!item.isSpecial) {
                Text(
                    text = item.label,
                    fontSize = 14.sp,
                    color = animatedTextColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = false, showSystemUi = true)
@Composable
private fun BottomBarFullScreenPreview() {
    val navController = rememberNavController()
    LogisticManagementApplicationTheme {
        Scaffold(
            bottomBar = {
                BottomBar(navController = navController)
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