
package com.example.diarioespacial.ui.asteroids

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AsteroidNavigation(
    viewModel: AsteroidViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "asteroid_list"
    ) {

        // Lista de asteroides
        composable("asteroid_list") {
            AsteroidListScreen(
                viewModel = viewModel,
                onAsteroidClick = { id ->
                    navController.navigate(
                        "asteroid_detail/$id"
                    )
                },
                onFavoritesClick = {
                    navController.navigate(
                        "asteroid_favorites"
                    )
                }
            )
        }

        // Detalle del asteroide
        composable(
            route = "asteroid_detail/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val asteroidId =
                backStackEntry.arguments
                    ?.getString("id")
                    .orEmpty()

            AsteroidDetailScreen(
                asteroidId = asteroidId,
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Asteroides favoritos
        composable("asteroid_favorites") {
            FavoriteAsteroidsScreen(
                viewModel = viewModel,
                onAsteroidClick = { id ->
                    navController.navigate(
                        "asteroid_detail/$id"
                    )
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
