package com.diogo.contasdacasa.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Profile

@Composable
fun ProfileSelectionScreen(
    profiles: List<Profile>,
    onProfileSelected: (Long) -> Unit,
    onCreateProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quem está usando?",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        profiles.forEach { profile ->
            OutlinedButton(
                onClick = {
                    onProfileSelected(profile.id)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = profile.name)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Criar novo perfil")
        }
    }
}