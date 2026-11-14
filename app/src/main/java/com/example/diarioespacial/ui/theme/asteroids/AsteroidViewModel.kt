
package com.example.diarioespacial.ui.asteroids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diarioespacial.data.local.AsteroidEntity
import com.example.diarioespacial.data.repository.AsteroidRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AsteroidViewModel(
    private val repository: AsteroidRepository
) : ViewModel() {

    // Lista completa de asteroides
    val asteroids: StateFlow<List<AsteroidEntity>> =
        repository.asteroids.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Asteroides peligrosos
    val hazardousAsteroids: StateFlow<List<AsteroidEntity>> =
        repository.hazardousAsteroids.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Asteroides favoritos
    val favoriteAsteroids: StateFlow<List<AsteroidEntity>> =
        repository.favoriteAsteroids.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Mensaje de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Filtro de peligrosidad
    private val _onlyHazardous = MutableStateFlow(false)
    val onlyHazardous: StateFlow<Boolean> = _onlyHazardous

    fun setOnlyHazardous(value: Boolean) {
        _onlyHazardous.value = value
    }

    // Descargar asteroides desde NASA
    fun refreshAsteroids() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                repository.refreshAsteroids()
            } catch (e: Exception) {
                _errorMessage.value =
                    "No se pudieron actualizar los asteroides: " +
                            (e.message ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Guardar o quitar favorito
    fun updateFavorite(id: String, favorite: Boolean) {
        viewModelScope.launch {
            try {
                repository.updateFavorite(id, favorite)
            } catch (e: Exception) {
                _errorMessage.value =
                    "No se pudo actualizar el favorito"
            }
        }
    }

    // Consultar detalle desde Room
    fun getAsteroidById(id: String) =
        repository.getAsteroidById(id)
}
