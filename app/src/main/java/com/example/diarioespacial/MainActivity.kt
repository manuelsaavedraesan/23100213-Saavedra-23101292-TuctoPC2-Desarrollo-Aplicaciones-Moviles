package com.example.diarioespacial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.diarioespacial.ui.theme.DiarioEspacialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getInstance(applicationContext)
        val api = NasaApiService.create()
        val apodRepository = ApodRepository(api, db.apodDao())

        setContent {
            DiarioEspacialTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentRoute?.startsWith("imagenes") == true,
                                onClick = {
                                    navController.navigate("imagenes_list") {
                                        popUpTo("imagenes_list") { inclusive = true }
                                    }
                                },
                                icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                                label = { Text("Imágenes") }
                            )
                            NavigationBarItem(
                                selected = currentRoute?.startsWith("asteroides") == true,
                                onClick = {
                                    navController.navigate("asteroides_list") {
                                        popUpTo("imagenes_list")
                                    }
                                },
                                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                label = { Text("Asteroides") }
                            )
                        }
                    }
                ) { innerPadding ->
                    val apodViewModel: ApodViewModel = viewModel(
                        factory = ApodViewModel.Factory(apodRepository)
                    )

                    NavHost(
                        navController = navController,
                        startDestination = "imagenes_list",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("imagenes_list") {
                            ApodListScreen(
                                viewModel = apodViewModel,
                                onItemClick = { date -> navController.navigate("imagenes_detail/$date") },
                                onOpenFavorites = { navController.navigate("imagenes_favorites") }
                            )
                        }
                        composable("imagenes_favorites") {
                            ApodFavoritesScreen(
                                viewModel = apodViewModel,
                                onBack = { navController.popBackStack() },
                                onItemClick = { date -> navController.navigate("imagenes_detail/$date") }
                            )
                        }
                        composable("imagenes_detail/{date}") { entry ->
                            val date = entry.arguments?.getString("date") ?: ""
                            ApodDetailScreen(
                                date = date,
                                viewModel = apodViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("asteroides_list") {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Módulo de Asteroides (Alumno 2)")
                            }
                        }
                    }
                }
            }
        }
    }
}