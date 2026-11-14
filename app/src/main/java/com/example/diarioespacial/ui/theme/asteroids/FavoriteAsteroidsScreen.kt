
package com.example.diarioespacial.ui.asteroids

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diarioespacial.data.local.AsteroidEntity
import java.util.Locale

@Composable
fun FavoriteAsteroidsScreen(
    viewModel: AsteroidViewModel,
    onAsteroidClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val favorites by viewModel.favoriteAsteroids.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Volver")
        }

        Text(
            text = "Mis asteroides",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            Text(
                text = "Todavía no tienes asteroides favoritos.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = favorites,
                    key = { it.id }
                ) { asteroid ->

                    FavoriteAsteroidItem(
                        asteroid = asteroid,
                        onClick = {
                            onAsteroidClick(asteroid.id)
                        },
                        onRemove = {
                            viewModel.updateFavorite(
                                asteroid.id,
                                false
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteAsteroidItem(
    asteroid: AsteroidEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = asteroid.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Fecha: ${asteroid.closeApproachDate}"
            )

            Text(
                "Diámetro máximo: ${
                    formatFavoriteNumber(asteroid.diameterMax)
                } m"
            )

            Text(
                "Velocidad: ${
                    formatFavoriteNumber(asteroid.velocityKmH)
                } km/h"
            )

            Text(
                "Distancia: ${
                    formatFavoriteNumber(asteroid.missDistanceKm)
                } km"
            )

            if (asteroid.isPotentiallyHazardous) {
                Text(
                    text = "⚠ Potencialmente peligroso",
                    color = MaterialTheme.colorScheme.error
                )
            }

            TextButton(onClick = onRemove) {
                Text("★ Quitar de favoritos")
            }
        }
    }
}

private fun formatFavoriteNumber(value: Double): String =
    String.format(Locale.US, "%.2f", value)
