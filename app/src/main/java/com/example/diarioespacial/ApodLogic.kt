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

        val nasaSpacePhotos = listOf(
            "https://images-assets.nasa.gov/image/PIA12348/PIA12348~medium.jpg",
            "https://images-assets.nasa.gov/image/PIA04921/PIA04921~medium.jpg",
            "https://images-assets.nasa.gov/image/PIA15415/PIA15415~medium.jpg",
            "https://images-assets.nasa.gov/image/PIA01341/PIA01341~medium.jpg",
            "https://images-assets.nasa.gov/image/PIA03149/PIA03149~medium.jpg",
            "https://images-assets.nasa.gov/image/PIA17563/PIA17563~medium.jpg",
            "https://images-assets.nasa.gov/image/PIA16884/PIA16884~medium.jpg"
        )

        val usedUrls = mutableSetOf<String>()
        val favDates = dao.getFavoriteDates().toSet()

        val entities = response.mapIndexedNotNull { index, dto ->
            val d = dto.date?.takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null
            val rawUrl = dto.url?.trim().orEmpty()
            val rawTitle = dto.title?.trim().orEmpty()
            val isGenericOrRepeated = rawUrl.isBlank() ||
                    dto.mediaType != "image" ||
                    rawTitle.equals("NASA Science", ignoreCase = true) ||
                    rawUrl.contains("logo", ignoreCase = true) ||
                    rawUrl in usedUrls

            val finalUrl = if (!isGenericOrRepeated) {
                rawUrl
            } else {
                nasaSpacePhotos[index % nasaSpacePhotos.size]
            }
            usedUrls.add(finalUrl)

            val finalTitle = if (rawTitle.isNotBlank() && !rawTitle.equals("NASA Science", ignoreCase = true)) {
                rawTitle
            } else {
                val snippet = dto.explanation
                    ?.substringBefore(".")
                    ?.trim()
                    ?.take(40)
                    ?.takeIf { it.isNotBlank() }
                if (snippet != null) "$snippet..." else "Fotografía Espacial ($d)"
            }

            dto.copy(
                title = finalTitle,
                url = finalUrl,
                mediaType = "image"
            ).toEntity(isFavorite = d in favDates)

        }

        if (entities.isNotEmpty()) {
            dao.clearNonFavorites()
            dao.insertAll(entities)
        }
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
                _error.value = "Error NASA: ${e.message ?: "revisa tu API Key"}"
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
