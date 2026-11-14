
package com.example.diarioespacial.ui.asteroids

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diarioespacial.data.repository.AsteroidRepository

class AsteroidViewModelFactory(
    private val repository: AsteroidRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(
                AsteroidViewModel::class.java
            )
        ) {
            return AsteroidViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido"
        )
    }
}
