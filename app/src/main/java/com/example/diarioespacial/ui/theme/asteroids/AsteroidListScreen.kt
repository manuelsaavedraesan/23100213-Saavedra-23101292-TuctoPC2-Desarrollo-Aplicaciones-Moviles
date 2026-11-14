
package com.example.diarioespacial.ui.asteroids

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diarioespacial.data.local.AsteroidEntity
import java.util.Locale

@Composable
fun AsteroidListScreen(
    viewModel: AsteroidViewModel,
    onAsteroidClick: (String) -> Unit,
    onFavoritesClick: () -> Unit
) {
    val asteroids by viewModel.asteroids.collectAsState()
    val onlyHazardous by viewModel.onlyHazardous.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshAsteroids()
    }

    val filteredAsteroids = remember(asteroids, onlyHazardous) {
        asteroids
            .filter { !onlyHazardous || it.isPotentiallyHazardous }
            .sortedByDescending { it.closeApproachDate }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Asteroides cercanos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFavoritesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("★ Mis asteroides favoritos")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Solo potencialmente peligrosos",
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = onlyHazardous,
                onCheckedChange = viewModel::setOnlyHazardous
            )
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error
            )

            TextButton(onClick = viewModel::refreshAsteroids) {
                Text("Reintentar")
            }
        }

        if (filteredAsteroids.isEmpty()) {
            if (!isLoading) {
                Text(
                    text = if (onlyHazardous) {
                        "No hay asteroides peligrosos para mostrar."
                    } else {
                        "No hay asteroides para mostrar."
                    }
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredAsteroids,
                    key = { it.id }
                ) { asteroid ->
                    AsteroidItem(
                        asteroid = asteroid,
                        onClick = { onAsteroidClick(asteroid.id) },
                        onFavoriteClick = {
                            viewModel.updateFavorite(
                                asteroid.id,
                                !asteroid.isFavorite
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AsteroidItem(
    asteroid: AsteroidEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = asteroid.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Fecha: ${asteroid.closeApproachDate}")

            Text(
                "Diámetro máximo: ${
                    formatNumber(asteroid.diameterMax)
                } m"
            )

            Text(
                "Velocidad: ${
                    formatNumber(asteroid.velocityKmH)
                } km/h"
            )

            Text(
                "Distancia: ${
                    formatNumber(asteroid.missDistanceKm)
                } km"
            )

            if (asteroid.isPotentiallyHazardous) {
                Text(
                    text = "⚠ Potencialmente peligroso",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = onFavoriteClick) {
                Text(
                    if (asteroid.isFavorite) {
                        "★ Quitar de favoritos"
                    } else {
                        "☆ Agregar a favoritos"
                    }
                )
            }
        }
    }
}

private fun formatNumber(value: Double): String =
    String.format(Locale.US, "%.2f", value)
