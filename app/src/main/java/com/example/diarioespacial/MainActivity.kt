
package com.example.diarioespacial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.diarioespacial.data.remote.NasaRetrofitClient
import com.example.diarioespacial.data.repository.AsteroidRepository
import com.example.diarioespacial.ui.asteroids.AsteroidNavigation
import com.example.diarioespacial.ui.asteroids.AsteroidViewModel
import com.example.diarioespacial.ui.asteroids.AsteroidViewModelFactory
import com.example.diarioespacial.ui.theme.DiarioEspacialTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Base de datos compartida: APOD y Asteroides
        val db = AppDatabase.getInstance(applicationContext)

        // Repositorio de imagenes APOD
        val apodApi = NasaApiService.create()
        val apodRepository = ApodRepository(
            apodApi,
            db.apodDao()
        )

        // Repositorio de asteroides NeoWs
        val asteroidRepository = AsteroidRepository(
            api = NasaRetrofitClient.api,
            dao = db.asteroidDao(),
            apiKey = NasaRetrofitClient.apiKey
        )

        setContent {
            DiarioEspacialTheme {

                val navController = rememberNavController()

                val backStackEntry by
                navController.currentBackStackEntryAsState()

                val currentRoute =
                    backStackEntry?.destination?.route

                // ViewModel de Imagenes
                val apodViewModel: ApodViewModel = viewModel(
                    factory = remember(apodRepository) {
                        ApodViewModel.Factory(apodRepository)
                    }
                )

                // ViewModel de Asteroides
                val asteroidViewModel: AsteroidViewModel = viewModel(
                    factory = remember(asteroidRepository) {
                        AsteroidViewModelFactory(asteroidRepository)
                    }
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {

                            // OPCION IMAGENES
                            NavigationBarItem(
                                selected = currentRoute
                                    ?.startsWith("imagenes") == true,

                                onClick = {
                                    navController.navigate("imagenes_list") {
                                        popUpTo("imagenes_list") {
                                            inclusive = false
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },

                                icon = {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = "Imagenes"
                                    )
                                },

                                label = {
                                    Text("Imágenes")
                                }
                            )

                            // OPCION ASTEROIDES
                            NavigationBarItem(
                                selected = currentRoute
                                    ?.startsWith("asteroides") == true,

                                onClick = {
                                    navController.navigate("asteroides_list") {
                                        popUpTo("imagenes_list") {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },

                                icon = {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = "Asteroides"
                                    )
                                },

                                label = {
                                    Text("Asteroides")
                                }
                            )
                        }
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = "imagenes_list",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        // ===== IMAGENES APOD =====

                        composable("imagenes_list") {
                            ApodListScreen(
                                viewModel = apodViewModel,
                                onItemClick = { date ->
                                    navController.navigate(
                                        "imagenes_detail/$date"
                                    )
                                },
                                onOpenFavorites = {
                                    navController.navigate(
                                        "imagenes_favorites"
                                    )
                                }
                            )
                        }

                        composable("imagenes_favorites") {
                            ApodFavoritesScreen(
                                viewModel = apodViewModel,
                                onBack = {
                                    navController.popBackStack()
                                },
                                onItemClick = { date ->
                                    navController.navigate(
                                        "imagenes_detail/$date"
                                    )
                                }
                            )
                        }

                        composable("imagenes_detail/{date}") { entry ->
                            val date = entry.arguments
                                ?.getString("date") ?: ""

                            ApodDetailScreen(
                                date = date,
                                viewModel = apodViewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ===== ASTEROIDES NEOWS =====

                        composable("asteroides_list") {
                            AsteroidNavigation(
                                viewModel = asteroidViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
