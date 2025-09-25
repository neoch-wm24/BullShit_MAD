package com.example.main_screen.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.main_screen.viewmodel.AuthState
import com.example.main_screen.viewmodel.AuthViewModel
import com.example.main_screen.viewmodel.LoginUiViewModel
import com.airbnb.lottie.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Lottie 动画
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.example.main_screen.R.raw.login_animation)
    )
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    // 使用 UI ViewModel 保存输入状态
    val uiViewModel: LoginUiViewModel = viewModel()
    val employeeID by uiViewModel.employeeId.collectAsState()
    val password by uiViewModel.password.collectAsState()
    val showPassword by uiViewModel.showPassword.collectAsState()
    val buttonPressed by uiViewModel.buttonPressed.collectAsState()

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    // 监听登录状态
    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                val role = state.role
                val id = state.employeeID
                navController.navigate("home/$role/$id") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }


    // 标题渐入
    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = LinearOutSlowInEasing),
        label = "titleAlpha"
    )

    // 按钮点击缩放
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lottie 动画
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(220.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 标题文字
        Text(
            text = "Login",
            fontSize = 32.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(titleAlpha)
        )

        Text(
            text = "Please sign in to continue.",
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.alpha(titleAlpha)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = employeeID,
            onValueChange = { uiViewModel.setEmployeeId(it) },
            label = { Text(text = "User ID") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { uiViewModel.setPassword(it) },
            label = { Text(text = "Password") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { authViewModel.loginWithEmployeeID(employeeID, password) }
            ),
            trailingIcon = {
                val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { uiViewModel.toggleShowPassword() }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 登录按钮
        Button(
            onClick = { authViewModel.loginWithEmployeeID(employeeID, password); uiViewModel.setButtonPressed(true) },
            enabled = authState.value != AuthState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .scale(
                    animateFloatAsState(
                        targetValue = if (buttonPressed) 0.95f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "buttonScale"
                    ).value
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { uiViewModel.setButtonPressed(true) },
        ) {
            AnimatedVisibility(visible = authState.value == AuthState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = authState.value != AuthState.Loading) {
                Text(text = "Sign in")
            }
        }
    }
}
