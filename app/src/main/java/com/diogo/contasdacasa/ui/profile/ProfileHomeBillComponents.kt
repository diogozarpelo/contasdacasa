package com.diogo.contasdacasa.ui.profile

import com.diogo.contasdacasa.data.model.BillEntryType

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diogo.contasdacasa.data.model.Bill
import com.diogo.contasdacasa.data.model.Profile
import com.diogo.contasdacasa.ui.util.formatCurrency
import com.diogo.contasdacasa.ui.util.formatMonthName

@Composable
internal fun AccountsSectionHeader(
    billCount: Int,
    selectedFilter: BillFilter,
    isAccountsVisible: Boolean,
    onFilterSelected: (BillFilter) -> Unit,
    onToggleAccountsVisibility: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Contas",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.size(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = billCount.toString(),
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 3.dp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = onToggleAccountsVisibility,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (isAccountsVisible) "Ocultar contas" else "Mostrar contas"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == BillFilter.ALL,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFilterSelected(BillFilter.ALL)
                    },
                    label = {
                        Text(text = "Todas")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = Color(0xFFE3F2FD)
                    )
                )

                FilterChip(
                    selected = selectedFilter == BillFilter.PENDING,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFilterSelected(BillFilter.PENDING)
                    },
                    label = {
                        Text(text = "Pendentes", maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelMedium)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = Color(0xFFFDECEC)
                    )
                )

                FilterChip(
                    selected = selectedFilter == BillFilter.PAID,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFilterSelected(BillFilter.PAID)
                    },
                    label = {
                        Text(text = "Pagas")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = Color(0xFFE5F4EA)
                    )
                )
            }
        }
    }
}
@Composable
internal fun BillCard(
    bill: Bill,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = if (bill.isPaid) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpanded),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (bill.isPaid) 0.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 10.dp
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bill.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.size(8.dp))

                PaymentStatusBadge(
                    isPaid = bill.isPaid
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (bill.amountInCents == 0L) {
                        "Valor não informado"
                    } else {
                        formatCurrency(bill.amountInCents)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (bill.amountInCents == 0L) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = if (bill.dueDay == 0) {
                        "Vencimento não informado"
                    } else {
                        "Vence dia ${bill.dueDay}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (bill.dueDay == 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isExpanded) {
                if (
                    bill.entryType == BillEntryType.INSTALLMENT &&
                    bill.installmentNumber != null &&
                    bill.totalInstallments != null
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Parcela ${bill.installmentNumber} de ${bill.totalInstallments}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onEdit
                    ) {
                        Text(text = "Editar")
                    }

                    TextButton(
                        onClick = onDelete
                    ) {
                        Text(
                            text = "Excluir",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable(
                                onClickLabel = if (bill.isPaid) {
                                    "Marcar como pendente"
                                } else {
                                    "Marcar como paga"
                                },
                                onClick = onTogglePaid
                            ),
                        color = if (bill.isPaid) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        shape = CircleShape
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (bill.isPaid) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onError
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentStatusBadge(
    isPaid: Boolean
) {
    Surface(
        color = if (isPaid) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = if (isPaid) {
                "Paga"
            } else {
                "Pendente"
            },
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            color = if (isPaid) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
internal fun EmptyAccountsCard(
    onAddBill: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nenhuma conta neste mês",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Adicione o primeiro lançamento para começar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onAddBill,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Adicionar conta")
            }
        }
    }
}
