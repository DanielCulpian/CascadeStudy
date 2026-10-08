package com.example.cascadestudy.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cascadestudy.domain.TimerState

private fun stateText(state: TimerState): String {
    return when (state) {
        TimerState.IDLE -> "Listo"
        TimerState.STUDYING -> "Estudiando"
        TimerState.RESTING -> "Descansando"
        TimerState.PAUSED -> "Pausado"
        TimerState.FINISHED -> "Finalizado"
    }
}

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(minutes, seconds)
}

private fun formatDisplayTime(hours: Float): String {
    val totalSeconds = (hours * 3600f).toLong()
    if (totalSeconds <= 0L) return "0m"

    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60

    return if (h > 0) {
        if (m > 0) "${h}h ${m}m" else "${h}h"
    } else {
        "${m}m"
    }
}

@Composable
fun TimerScreen(
    uiState: TimerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onFinishEarly: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Histórico",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                    Text(
                        text = formatDisplayTime(uiState.totalHistoricalHours),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Esta Semana",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )
                    Text(
                        text = formatDisplayTime(uiState.weeklyHours),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 24.dp,
                        vertical = 32.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stateText(uiState.state),
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = formatTime(uiState.remainingSeconds),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Intervalo ${uiState.currentIntervalIndex + 1} de ${uiState.totalIntervals}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (uiState.state == TimerState.IDLE) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Iniciar")
                }
            }

            if (uiState.state == TimerState.STUDYING ||
                uiState.state == TimerState.RESTING
            ) {
                Button(
                    onClick = onPause,
                    modifier = Modifier
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Pausar")
                }
            }

            if (uiState.state == TimerState.PAUSED) {
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Reanudar")
                }
            }

            if (uiState.state == TimerState.PAUSED ||
                uiState.state == TimerState.FINISHED
            ) {
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        "Reiniciar",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (uiState.state == TimerState.STUDYING ||
                uiState.state == TimerState.RESTING ||
                uiState.state == TimerState.PAUSED
            ) {
                OutlinedButton(
                    onClick = onFinishEarly,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        "Finalizar",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
