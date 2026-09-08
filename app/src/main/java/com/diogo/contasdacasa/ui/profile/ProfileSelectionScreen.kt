package com.diogo.contasdacasa.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 28.dp,
            end = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SelectionHeader()
        }

        errorMessage?.let { message ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        items(
            items = profiles,
            key = { profile -> profile.id }
        ) { profile ->
            ProfileCard(
                profile = profile,
                isSaving = isSaving,
                onEnter = {
                    onProfileSelected(profile.id)
                },
                onRename = {
                    editedName = profile.name
                    profileBeingRenamed = profile
                },
                onDelete = {
                    profilePendingDeletion = profile
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))

            OutlinedButton(
                onClick = onCreateProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Criar novo perfil",
                    style = MaterialTheme.typography.labelLarge
                )
            }
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
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Nome")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
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
                Button(
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

@Composable
private fun SelectionHeader() {
    Text(
        text = "Quem está usando?",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(5.dp))

    Text(
        text = "Escolha um perfil para acessar as contas.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(6.dp))
}
@Composable
private fun ProfileCard(
    profile: Profile,
    isSaving: Boolean,
    onEnter: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name
                                .trim()
                                .take(1)
                                .uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.size(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Perfil financeiro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEnter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(13.dp)
            ) {
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onRename,
                    enabled = !isSaving
                ) {
                    Text(text = "Renomear")
                }

                TextButton(
                    onClick = onDelete,
                    enabled = !isSaving
                ) {
                    Text(
                        text = "Excluir",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}