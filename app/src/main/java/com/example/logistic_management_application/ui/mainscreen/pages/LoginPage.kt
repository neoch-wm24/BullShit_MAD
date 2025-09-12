package com.example.logistic_management_application.ui.mainscreen.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.logistic_management_application.ui.modules.user.AuthViewModel
import com.example.logistic_management_application.R
import com.example.logistic_management_application.ui.modules.user.AuthState

@Composable
fun LoginPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val loginPageImage = painterResource(R.drawable.login_page_image)

    var employeeID by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }

    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Image(
            painter = loginPageImage,
            contentDescription = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Login",
            fontSize = 32.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
        )

        Text(text = "Please sign in to continue.",
            fontSize = 15.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = employeeID,
            onValueChange = {
                employeeID = it
            },
            label = {
                Text(text = "Employee ID")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text(text = "Password")
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { authViewModel.login(employeeID, password) },
            enabled = authState.value != AuthState.Loading,
            modifier = Modifier
                .width(280.dp)
                .height(40.dp)
        ) {
            Text(text = "Sign in")
        }

        Spacer(modifier = Modifier.height(15.dp))

        TextButton(onClick = {

        }) {
            Text(text = "Forget Password?")
        }
    }
}
