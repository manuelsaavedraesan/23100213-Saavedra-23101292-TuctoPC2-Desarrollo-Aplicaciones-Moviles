package com.example.diarioespacial

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApodListScreen(
    viewModel: ApodViewModel,
    onItemClick: (String) -> Unit,
    onOpenFavorites: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Imágenes (APOD)") },
                actions = {
                    TextButton(onClick = onOpenFavorites) {
                        Icon(Icons.Filled.Favorite, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Mis imágenes favoritas")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.items.isEmpty() -> {
                    Text(
                        text = state.errorMessage ?: "No hay imágenes disponibles para mostrar.",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items, key = { it.date }) { item ->
                            ApodCard(
                                item = item,
                                onClick = { onItemClick(item.date) },
                                onToggleFavorite = { viewModel.toggleFavorite(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApodCard(
    item: ApodEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column {
            if (item.mediaType == "video") {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Contenido en video", style = MaterialTheme.typography.titleMedium)
                }
            } else if (item.url.isNotBlank()) {
                AsyncImage(
                    model = item.url,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(item.date, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApodFavoritesScreen(
    viewModel: ApodViewModel,
    onBack: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis imágenes favoritas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.favorites.isEmpty()) {
                Text(
                    text = "Aún no tienes imágenes marcadas como favoritas.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.favorites, key = { it.date }) { item ->
                        ApodCard(
                            item = item,
                            onClick = { onItemClick(item.date) },
                            onToggleFavorite = { viewModel.toggleFavorite(item) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApodDetailScreen(
    date: String,
    viewModel: ApodViewModel,
    onBack: () -> Unit
) {
    val item by viewModel.observeDetail(date).collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    item?.let { current ->
                        IconButton(onClick = { viewModel.toggleFavorite(current) }) {
                            Icon(
                                imageVector = if (current.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (current.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val current = item
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No se encontró la publicación.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (current.mediaType == "video") {
                    Text("Contenido en video", style = MaterialTheme.typography.titleMedium)
                } else {
                    val imageToLoad = current.hdUrl?.takeIf { it.isNotBlank() } ?: current.url
                    if (imageToLoad.isNotBlank()) {
                        AsyncImage(
                            model = imageToLoad,
                            contentDescription = current.title,
                            modifier = Modifier.fillMaxWidth().height(260.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(current.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Fecha: ${current.date}", style = MaterialTheme.typography.bodyMedium)
                current.copyright?.let { author ->
                    Text("Autor: $author", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(12.dp))
                Text(current.explanation, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}