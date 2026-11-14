package com.example.diarioespacial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Conversión explícita de DTO (API) a Entity (Room)
fun ApodDto.toEntity(isFavorite: Boolean): ApodEntity? {
    val safeDate = date?.takeIf { it.isNotBlank() } ?: return null
    return ApodEntity(
        date = safeDate,
        title = title?.takeIf { it.isNotBlank() } ?: "Sin título",
        explanation = explanation?.takeIf { it.isNotBlank() } ?: "Sin explicación disponible.",
        url = url ?: "",
        mediaType = mediaType ?: "image",
        hdUrl = hdUrl,
        copyright = copyright?.trim()?.takeIf { it.isNotBlank() },
        isFavorite = isFavorite
    )
}

class ApodRepository(
    private val api: NasaApiService,
    private val dao: ApodDao
) {
    val allApods: Flow<List<ApodEntity>> = dao.observeAll()
    val favoriteApods: Flow<List<ApodEntity>> = dao.observeFavorites()
    fun apodByDate(date: String): Flow<ApodEntity?> = dao.observeByDate(date)

    suspend fun refreshLast7Days() {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        val sixDaysAgo = today.minusDays(6)
        val response = api.getApodRange(
            startDate = sixDaysAgo.format(formatter),
            endDate = today.format(formatter)
        )
        val favDates = dao.getFavoriteDates().toSet()
        val entities = response.mapNotNull { dto ->
            val d = dto.date ?: return@mapNotNull null
            dto.toEntity(isFavorite = d in favDates)
        }
        dao.insertAll(entities)
    }

    suspend fun toggleFavorite(date: String, currentState: Boolean) {
        dao.updateFavorite(date, !currentState)
    }
}

data class ApodUiState(
    val items: List<ApodEntity> = emptyList(),
    val favorites: List<ApodEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ApodViewModel(private val repository: ApodRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ApodUiState> = combine(
        repository.allApods,
        repository.favoriteApods,
        _isLoading,
        _error
    ) { items, favs, loading, error ->
        ApodUiState(
            items = items,
            favorites = favs,
            isLoading = loading,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApodUiState(isLoading = true))

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.refreshLast7Days()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar los datos de la NASA."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun observeDetail(date: String): StateFlow<ApodEntity?> =
        repository.apodByDate(date).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun toggleFavorite(item: ApodEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.date, item.isFavorite)
        }
    }

    class Factory(private val repository: ApodRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ApodViewModel(repository) as T
    }
}
