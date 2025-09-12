package com.example.logistic_management_application.ui.modules.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import com.example.logistic_management_application.R
import com.example.logistic_management_application.ui.mainscreen.navbottombar.NavBottomBar


@Composable
fun UserScreen(modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(
        topBar = {
            UserTitle(navController = navController)
        },
        bottomBar = {
            NavBottomBar(modifier, navController)
        }
    ) { innerPadding ->
        // 中间内容区域

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F8F8))
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
fun UserTitle(
    title: String = stringResource(id = R.string.user_pt),
    navController: NavController
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
            IconButton(onClick = {navController.popBackStack()}) {
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