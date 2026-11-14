
package com.example.diarioespacial.ui.asteroids

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AsteroidDetailScreen(
    asteroidId: String,
    viewModel: AsteroidViewModel,
    onBack: () -> Unit
) {
    val asteroidFlow = remember(asteroidId) {
        viewModel.getAsteroidById(asteroidId)
    }

    val asteroid by asteroidFlow.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Volver")
        }

        if (asteroid == null) {
            Text("No se encontró el asteroide.")
        } else {
            val item = asteroid!!

            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("ID: ${item.id}")
            Text("Fecha de aproximación: ${item.closeApproachDate}")

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                "Diámetro estimado",
                fontWeight = FontWeight.Bold
            )

            Text("Mínimo: ${formatDetail(item.diameterMin)} m")
            Text("Máximo: ${formatDetail(item.diameterMax)} m")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Velocidad: ${formatDetail(item.velocityKmH)} km/h"
            )
            Text(
                "Distancia de paso: ${
                    formatDetail(item.missDistanceKm)
                } km"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (item.isPotentiallyHazardous) {
                    "⚠ Potencialmente peligroso"
                } else {
                    "No es potencialmente peligroso"
                },
                color = if (item.isPotentiallyHazardous) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.updateFavorite(
                        item.id,
                        !item.isFavorite
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (item.isFavorite) {
                        "★ Quitar de favoritos"
                    } else {
                        "☆ Agregar a favoritos"
                    }
                )
            }
        }
    }
}

private fun formatDetail(value: Double): String =
    String.format(Locale.US, "%.2f", value)
