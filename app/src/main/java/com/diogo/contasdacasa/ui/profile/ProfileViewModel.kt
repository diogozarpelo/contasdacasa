package com.diogo.contasdacasa.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.diogo.contasdacasa.data.model.Profile
import com.diogo.contasdacasa.data.repository.ProfileRepository
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val createdProfileId: Long? = null,
    val createdProfileName: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                val profiles = repository.getAllProfiles()

                uiState = uiState.copy(
                    profiles = profiles,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os perfis."
                )
            }
        }
    }

    fun createProfile(name: String) {
        val normalizedName = name.trim()

        if (normalizedName.isBlank() || uiState.isSaving) {
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(
                isSaving = true,
                createdProfileId = null,
                createdProfileName = null,
                errorMessage = null
            )

            try {
                val profileId = repository.createProfile(normalizedName)
                val profiles = repository.getAllProfiles()

                uiState = uiState.copy(
                    profiles = profiles,
                    isSaving = false,
                    createdProfileId = profileId,
                    createdProfileName = normalizedName
                )
            } catch (_: Exception) {
                uiState = uiState.copy(
                    isSaving = false,
                    errorMessage = "Não foi possível criar o perfil."
                )
            }
        }
    }

    fun clearFeedback() {
        uiState = uiState.copy(
            createdProfileId = null,
            createdProfileName = null,
            errorMessage = null
        )
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