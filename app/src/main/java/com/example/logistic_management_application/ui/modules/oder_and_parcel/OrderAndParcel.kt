package com.example.logistic_management_application.ui.modules.oder_and_parcel

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.logistic_management_application.R

@Composable
fun OrderAndParcelScreen(modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(
        topBar = {
            OrderAndParcelPageTitle(
                onBackClick = { /* 返回逻辑 */ }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onProfileClick = { /* Profile 点击 */ },
                onHomeClick = { /* Home 点击 */ },
                onSettingsClick = { /* Settings 点击 */ }
            )
        }
    ) { innerPadding ->
        // 中间内容区域

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8)) // 只是示例背景色
        ){
            ExpandableFab(
                onFirstClick = { /* 点击第一个按钮逻辑 */ },
                onSecondClick = { /* 点击第二个按钮逻辑 */ },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun OrderAndParcelPageTitle(
    title: String = stringResource(id = R.string.order_and_parcel_pt),
    onBackClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White) // 整个标题栏背景
            .drawBehind { // 底部下划线
                val strokeWidth = 2.dp.toPx()
                drawLine(
                    color = Color(0xFFDDDDDD), // 灰色分隔线
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
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ExpandableFab(
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 子按钮组：固定在右下角，并且往上展开
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)   // 整个组固定在右下角
                .padding(end = 24.dp, bottom = 24.dp)
        ) {
            if (expanded) {
                CircleNavButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Orders",
                    selected = false,
                    onClick = onFirstClick
                )
                CircleNavButton(
                    icon = Icons.Default.Add,
                    contentDescription = "Add",
                    selected = false,
                    onClick = onSecondClick
                )
            }

            // 主按钮永远在最下方
            CircleNavButton(
                icon = if (expanded) Icons.Default.Close else Icons.Default.Build,
                contentDescription = "Toggle",
                selected = false,
                onClick = { expanded = !expanded }
            )
        }
    }
}

@Composable
fun CircleNavButton(
    icon: ImageVector,
    contentDescription: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (selected) Color.Black else Color.Transparent)
            .border(2.dp, Color.Black, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) Color.White else Color.Black,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun BottomNavigationBar(
    onProfileClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White) // 整个底部区域背景
            .navigationBarsPadding() // 确保到底
            .drawBehind { // 顶部分隔线
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
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationButton(Icons.Default.Person, "Profile", onProfileClick, Modifier.weight(1f))
            NavigationButton(Icons.Default.Home, "Home", onHomeClick, Modifier.weight(1f))
            NavigationButton(Icons.Default.Settings, "Settings", onSettingsClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavigationButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(26.dp),
            tint = Color(0xFF555555)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF555555)
        )
    }
}