package com.diogo.contasdacasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diogo.contasdacasa.data.local.AppDatabase
import com.diogo.contasdacasa.data.repository.ProfileRepository
import com.diogo.contasdacasa.ui.profile.ProfileCreationScreen
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

            ContasDaCasaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    ProfileCreationScreen(
                        uiState = profileViewModel.uiState,
                        onCreateProfile = profileViewModel::createProfile,
                        onClearFeedback = profileViewModel::clearFeedback,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}