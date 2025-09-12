package screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import data.DeliveryViewModel

@Composable
fun DeliveryAndTransportationNavHost(
    mainNavController: NavController,
    modifier: Modifier = Modifier
) {
    val navController: NavHostController = rememberNavController()
    val deliveryViewModel: DeliveryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "delivery_list",
        modifier = modifier
    ) {
        composable("delivery_list") {
            DeliveryScreen(
                deliveries = deliveryViewModel.deliveries,
                onAddDelivery = {
                    navController.navigate("add_transportation")
                },
                navController = navController,
                deliveryViewModel = deliveryViewModel
            )
        }

        composable("add_transportation") {
            AddTransportationScreen(
                navController = navController,
                onSave = { delivery ->
                    deliveryViewModel.addDelivery(delivery)
                }
            )
        }
    }
}
