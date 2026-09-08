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
import com.diogo.contasdacasa.ui.profile.ProfileCreationScreen
import com.diogo.contasdacasa.ui.profile.ProfileHomeScreen
import com.diogo.contasdacasa.ui.profile.ProfileSelectionScreen
import com.diogo.contasdacasa.ui.profile.ProfileViewModel
import com.diogo.contasdacasa.ui.splash.BrandedSplashScreen
import com.diogo.contasdacasa.ui.theme.ContasDaCasaTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.delay

@Composable
internal fun ContasDaCasaApp(
    profileRepository: ProfileRepository,
    billRepository: BillRepository
) {
val profileViewModel: ProfileViewModel = viewModel(
                factory = ProfileViewModel.Factory(profileRepository)
            )

            val billViewModel: BillViewModel = viewModel(
                factory = BillViewModel.Factory(billRepository)
            )

            val profileUiState = profileViewModel.uiState
            val billUiState = billViewModel.uiState

            var destination by rememberSaveable {
                mutableStateOf(AppDestination.HOME)
            }
            var editingBillId by rememberSaveable {
                mutableStateOf<Long?>(null)
            }

            var selectedProfileId by rememberSaveable {
                mutableStateOf<Long?>(null)
            }

            var isShowingBrandSplash by rememberSaveable {
                mutableStateOf(true)
            }



            LaunchedEffect(Unit) {
                delay(3000L)
                isShowingBrandSplash = false
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
                    destination = AppDestination.HOME
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
                    destination = AppDestination.HOME
                    billViewModel.clearFeedback()
                }
            }


            LaunchedEffect(billUiState.wasNextMonthPrepared) {
                if (billUiState.wasNextMonthPrepared) {
                    destination = AppDestination.HOME
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
                if (isShowingBrandSplash) {
                    BrandedSplashScreen()
                } else {
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

                        selectedProfile != null && destination == AppDestination.PREPARE_NEXT_MONTH -> {
                            PrepareNextMonthScreen(
                                bills = billUiState.bills,
                                sourceMonth = billUiState.month,
                                sourceYear = billUiState.year,
                                isSaving = billUiState.isSaving,
                                errorMessage = billUiState.errorMessage,
                                onConfirm = billViewModel::prepareNextMonth,
                                onCancel = {
                                    billViewModel.clearFeedback()
                                    destination = AppDestination.HOME
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        selectedProfile != null &&
                            destination == AppDestination.EDIT_BILL &&
                            editingBill != null -> {
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

                        selectedProfile != null && destination == AppDestination.CREATE_BILL -> {
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
                                    destination = AppDestination.HOME
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
                                    destination = AppDestination.PREPARE_NEXT_MONTH
                                },
                                onAddBill = {
                                    billViewModel.clearFeedback()
                                    destination = AppDestination.CREATE_BILL
                                },
                                onEditBill = { bill ->
                                    billViewModel.clearFeedback()
                                    editingBillId = bill.id
                                    destination = AppDestination.EDIT_BILL
                                },
                                onTogglePaid = billViewModel::togglePaid,
                                onDeleteBill = billViewModel::deleteBill,
                                onDeleteInstallmentsFromCurrent =
                                    billViewModel::deleteInstallmentsFromCurrent,
                                onChangeProfile = {
                                    destination = AppDestination.HOME
                                    editingBillId = null
                                    selectedProfileId = null
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        destination == AppDestination.CREATE_PROFILE ||
                            profileUiState.profiles.isEmpty() -> {
                            ProfileCreationScreen(
                                uiState = profileUiState,
                                onCreateProfile = profileViewModel::createProfile,
                                onClearFeedback = profileViewModel::clearFeedback,
                                onCancel = if (profileUiState.profiles.isNotEmpty()) {
                                    {
                                        profileViewModel.clearFeedback()
                                        destination = AppDestination.HOME
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
                                    destination = AppDestination.CREATE_PROFILE
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