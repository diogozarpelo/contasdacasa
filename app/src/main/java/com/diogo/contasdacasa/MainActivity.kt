package com.diogo.contasdacasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diogo.contasdacasa.data.local.AppDatabase
import com.diogo.contasdacasa.data.repository.ProfileRepository
import com.diogo.contasdacasa.ui.profile.ProfileCreationScreen
import com.diogo.contasdacasa.ui.profile.ProfileHomeScreen
import com.diogo.contasdacasa.ui.profile.ProfileSelectionScreen
import com.diogo.contasdacasa.ui.profile.ProfileViewModel
import com.diogo.contasdacasa.ui.theme.ContasDaCasaTheme

class MainActivity : ComponentActivity() {

    private val profileRepository by lazy {
        ProfileRepository(
            AppDatabase
                .getInstance(applicationContext)
                .profileDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepository)
            )

            val uiState = profileViewModel.uiState

            var isCreatingProfile by rememberSaveable {
                mutableStateOf(false)
            }

            var selectedProfileId by rememberSaveable {
                mutableStateOf<Long?>(null)
            }

            val selectedProfile = uiState.profiles.firstOrNull { profile ->
                profile.id == selectedProfileId
            }

            LaunchedEffect(uiState.createdProfileId) {
                uiState.createdProfileId?.let { profileId ->
                    selectedProfileId = profileId
                    isCreatingProfile = false
                    profileViewModel.clearFeedback()
                }
            }

            ContasDaCasaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        selectedProfile != null -> {
                            ProfileHomeScreen(
                                profile = selectedProfile,
                                onChangeProfile = {
                                    selectedProfileId = null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        isCreatingProfile || uiState.profiles.isEmpty() -> {
                            ProfileCreationScreen(
                                uiState = uiState,
                                onCreateProfile = profileViewModel::createProfile,
                                onClearFeedback = profileViewModel::clearFeedback,
                                onCancel = if (uiState.profiles.isNotEmpty()) {
                                    {
                                        profileViewModel.clearFeedback()
                                        isCreatingProfile = false
                                    }
                                } else {
                                    null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        else -> {
                            ProfileSelectionScreen(
                                profiles = uiState.profiles,
                                onProfileSelected = { profileId ->
                                    selectedProfileId = profileId
                                },
                                onCreateProfile = {
                                    profileViewModel.clearFeedback()
                                    isCreatingProfile = true
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}