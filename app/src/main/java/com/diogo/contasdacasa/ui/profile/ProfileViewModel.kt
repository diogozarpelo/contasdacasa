package com.diogo.contasdacasa.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.diogo.contasdacasa.data.repository.ProfileRepository
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isSaving: Boolean = false,
    val createdProfileName: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    fun createProfile(name: String) {
        val normalizedName = name.trim()

        if (normalizedName.isBlank() || uiState.isSaving) {
            return
        }

        viewModelScope.launch {
            uiState = ProfileUiState(isSaving = true)

            try {
                repository.createProfile(normalizedName)

                uiState = ProfileUiState(
                    createdProfileName = normalizedName
                )
            } catch (_: Exception) {
                uiState = ProfileUiState(
                    errorMessage = "Não foi possível criar o perfil."
                )
            }
        }
    }

    fun clearFeedback() {
        if (
            uiState.createdProfileName != null ||
            uiState.errorMessage != null
        ) {
            uiState = ProfileUiState()
        }
    }

    class Factory(
        private val repository: ProfileRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                return ProfileViewModel(repository) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}