package com.example.main_screen.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.core_resources.R
import com.example.main_screen.viewmodel.AuthState
import com.example.main_screen.viewmodel.AuthViewModel

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val loginPageImage = painterResource(R.drawable.login_page_image)

    var employeeID by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when (val state = authState.value) {
            is AuthState.Authenticated -> {
                // 修复：统一使用 "home" 路由，在 HomePage 中根据 role 显示不同内容
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = loginPageImage, contentDescription = null)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Login",
            fontSize = 32.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Please sign in to continue.",
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = employeeID,
            onValueChange = { employeeID = it },
            label = { Text(text = "Employee ID") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { authViewModel.loginWithEmployeeID(employeeID, password) }
            ),
            trailingIcon = {
                val image =
                    if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { authViewModel.loginWithEmployeeID(employeeID, password) },
            enabled = authState.value != AuthState.Loading,
            modifier = Modifier
                .width(280.dp)
                .height(40.dp)
        ) {
            Text(text = "Sign in")
        }

        Spacer(modifier = Modifier.height(15.dp))

        TextButton(onClick = { /* forgot password logic */ }) {
            Text(text = "Forget Password?")
        }
    }
}