package com.diogo.contasdacasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.diogo.contasdacasa.data.local.AppDatabase
import com.diogo.contasdacasa.data.repository.BillRepository
import com.diogo.contasdacasa.data.repository.ProfileRepository

class MainActivity : ComponentActivity() {

    private val database by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    private val profileRepository by lazy {
        ProfileRepository(database.profileDao())
    }

    private val billRepository by lazy {
        BillRepository(database.billDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ContasDaCasa)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ContasDaCasaApp(
                profileRepository = profileRepository,
                billRepository = billRepository
            )
        }
    }
}