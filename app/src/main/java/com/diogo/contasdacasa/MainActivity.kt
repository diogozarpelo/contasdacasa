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
import com.diogo.contasdacasa.data.repository.BillRepository
import com.diogo.contasdacasa.data.repository.ProfileRepository
import com.diogo.contasdacasa.ui.bill.BillCreationScreen
import com.diogo.contasdacasa.ui.bill.BillEditScreen
import com.diogo.contasdacasa.ui.bill.BillViewModel
import com.diogo.contasdacasa.ui.bill.PrepareNextMonthScreen
import com.diogo.contasdacasa.ui.bill.PrepareNextMonthScreen
import com.diogo.contasdacasa.ui.profile.ProfileCreationScreen
import com.diogo.contasdacasa.ui.profile.ProfileHomeScreen
import com.diogo.contasdacasa.ui.profile.ProfileSelectionScreen
import com.diogo.contasdacasa.ui.profile.ProfileViewModel
import com.diogo.contasdacasa.ui.theme.ContasDaCasaTheme

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
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepository)
            )

            val billViewModel: BillViewModel = viewModel(
                factory = BillViewModel.Factory(billRepository)
            )

            val profileUiState = profileViewModel.uiState
            val billUiState = billViewModel.uiState

            var isCreatingProfile by rememberSaveable {
                mutableStateOf(false)
            }

            var isCreatingBill by rememberSaveable {
                mutableStateOf(false)
            }

            var isPreparingNextMonth by rememberSaveable {
                mutableStateOf(false)
            }

            var editingBillId by rememberSaveable {
                mutableStateOf<Long?>(null)
            }

            var selectedProfileId by rememberSaveable {
                mutableStateOf<Long?>(null)
            }

            val selectedProfile = profileUiState.profiles.firstOrNull { profile ->
                profile.id == selectedProfileId
            }

            val editingBill = billUiState.bills.firstOrNull { bill ->
                bill.id == editingBillId
            }

            LaunchedEffect(profileUiState.createdProfileId) {
                profileUiState.createdProfileId?.let { profileId ->
                    selectedProfileId = profileId
                    isCreatingProfile = false
                    profileViewModel.clearFeedback()
                }
            }

            LaunchedEffect(selectedProfileId) {
                selectedProfileId?.let { profileId ->
                    billViewModel.openProfile(profileId)
                }
            }

            LaunchedEffect(billUiState.wasBillCreated) {
                if (billUiState.wasBillCreated) {
                    isCreatingBill = false
                    billViewModel.clearFeedback()
                }
            }


            LaunchedEffect(billUiState.wasNextMonthPrepared) {
                if (billUiState.wasNextMonthPrepared) {
                    isPreparingNextMonth = false
                    billViewModel.clearFeedback()
                }
            }

            LaunchedEffect(billUiState.wasNextMonthPrepared) {
                if (billUiState.wasNextMonthPrepared) {
                    isPreparingNextMonth = false
                    billViewModel.clearFeedback()
                }
            }
            LaunchedEffect(billUiState.wasBillUpdated) {
                if (billUiState.wasBillUpdated) {
                    editingBillId = null
                    billViewModel.clearFeedback()
                }
            }

            ContasDaCasaTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when {
                        profileUiState.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        selectedProfile != null && isPreparingNextMonth -> {
                            PrepareNextMonthScreen(
                                bills = billUiState.bills,
                                sourceMonth = billUiState.month,
                                sourceYear = billUiState.year,
                                isSaving = billUiState.isSaving,
                                errorMessage = billUiState.errorMessage,
                                onConfirm = billViewModel::prepareNextMonth,
                                onCancel = {
                                    billViewModel.clearFeedback()
                                    isPreparingNextMonth = false
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        selectedProfile != null && isPreparingNextMonth -> {
                            PrepareNextMonthScreen(
                                bills = billUiState.bills,
                                sourceMonth = billUiState.month,
                                sourceYear = billUiState.year,
                                isSaving = billUiState.isSaving,
                                errorMessage = billUiState.errorMessage,
                                onConfirm = billViewModel::prepareNextMonth,
                                onCancel = {
                                    billViewModel.clearFeedback()
                                    isPreparingNextMonth = false
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        selectedProfile != null && editingBill != null -> {
                            BillEditScreen(
                                bill = editingBill,
                                uiState = billUiState,
                                onSave = billViewModel::updateBillDetails,
                                onSaveFromCurrent = billViewModel::updateInstallmentsFromCurrent,
                                onClearFeedback = billViewModel::clearFeedback,
                                onCancel = {
                                    billViewModel.clearFeedback()
                                    editingBillId = null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        selectedProfile != null && isCreatingBill -> {
                            BillCreationScreen(
                                uiState = billUiState,
                                onCreateMonthlyBill = { name, amount, dueDay ->
                                    billViewModel.createBill(
                                        name = name,
                                        amountText = amount,
                                        dueDayText = dueDay,
                                    )
                                },
                                onCreateInstallmentPlan = billViewModel::createInstallmentPlan,
                                onClearFeedback = billViewModel::clearFeedback,
                                onCancel = {
                                    billViewModel.clearFeedback()
                                    isCreatingBill = false
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        selectedProfile != null -> {
                            ProfileHomeScreen(
                                profile = selectedProfile,
                                bills = billUiState.bills,
                                month = billUiState.month,
                                year = billUiState.year,
                                isLoading = billUiState.isLoading,
                                errorMessage = billUiState.errorMessage,
                                onPreviousMonth = {
                                    billViewModel.changeMonth(-1)
                                },
                                onNextMonth = {
                                    billViewModel.changeMonth(1)
                                },
                                onPrepareNextMonth = {
                                    billViewModel.clearFeedback()
                                    isPreparingNextMonth = true
                                },
                                onAddBill = {
                                    billViewModel.clearFeedback()
                                    isCreatingBill = true
                                },
                                onEditBill = { bill ->
                                    billViewModel.clearFeedback()
                                    editingBillId = bill.id
                                },
                                onTogglePaid = billViewModel::togglePaid,
                                onDeleteBill = billViewModel::deleteBill,
                                onDeleteInstallmentsFromCurrent =
                                    billViewModel::deleteInstallmentsFromCurrent,
                                onChangeProfile = {
                                    isCreatingBill = false
                                    isPreparingNextMonth = false
                                    editingBillId = null
                                    selectedProfileId = null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        isCreatingProfile || profileUiState.profiles.isEmpty() -> {
                            ProfileCreationScreen(
                                uiState = profileUiState,
                                onCreateProfile = profileViewModel::createProfile,
                                onClearFeedback = profileViewModel::clearFeedback,
                                onCancel = if (profileUiState.profiles.isNotEmpty()) {
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
                                profiles = profileUiState.profiles,
                                isSaving = profileUiState.isSaving,
                                errorMessage = profileUiState.errorMessage,
                                onProfileSelected = { profileId ->
                                    selectedProfileId = profileId
                                },
                                onCreateProfile = {
                                    profileViewModel.clearFeedback()
                                    isCreatingProfile = true
                                },
                                onRenameProfile = profileViewModel::renameProfile,
                                onDeleteProfile = profileViewModel::deleteProfile,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
