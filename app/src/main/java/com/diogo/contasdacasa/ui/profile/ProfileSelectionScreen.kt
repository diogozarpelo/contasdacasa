package com.diogo.contasdacasa.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Profile

@Composable
fun ProfileSelectionScreen(
    profiles: List<Profile>,
    isSaving: Boolean,
    errorMessage: String?,
    onProfileSelected: (Long) -> Unit,
    onCreateProfile: () -> Unit,
    onRenameProfile: (Profile, String) -> Unit,
    onDeleteProfile: (Profile) -> Unit,
    modifier: Modifier = Modifier
) {
    var profileBeingRenamed by remember {
        mutableStateOf<Profile?>(null)
    }

    var editedName by remember {
        mutableStateOf("")
    }

    var profilePendingDeletion by remember {
        mutableStateOf<Profile?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Quem está usando?",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(20.dp))

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        profiles.forEach { profile ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            onProfileSelected(profile.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        Text(text = "Entrar")
                    }

                    Row {
                        TextButton(
                            onClick = {
                                editedName = profile.name
                                profileBeingRenamed = profile
                            },
                            enabled = !isSaving
                        ) {
                            Text(text = "Renomear")
                        }

                        TextButton(
                            onClick = {
                                profilePendingDeletion = profile
                            },
                            enabled = !isSaving
                        ) {
                            Text(text = "Excluir")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateProfile,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(text = "Criar novo perfil")
        }
    }

    profileBeingRenamed?.let { profile ->
        AlertDialog(
            onDismissRequest = {
                profileBeingRenamed = null
            },
            title = {
                Text(text = "Renomear perfil")
            },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                    },
                    label = {
                        Text(text = "Nome")
                    },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRenameProfile(
                            profile,
                            editedName
                        )
                        profileBeingRenamed = null
                    },
                    enabled = editedName.isNotBlank() && !isSaving
                ) {
                    Text(text = "Salvar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        profileBeingRenamed = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }

    profilePendingDeletion?.let { profile ->
        AlertDialog(
            onDismissRequest = {
                profilePendingDeletion = null
            },
            title = {
                Text(text = "Excluir perfil?")
            },
            text = {
                Text(
                    text = "Todas as contas e parcelas do perfil ${profile.name} também serão excluídas."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProfile(profile)
                        profilePendingDeletion = null
                    },
                    enabled = !isSaving
                ) {
                    Text(text = "Excluir perfil")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        profilePendingDeletion = null
                    }
                ) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}