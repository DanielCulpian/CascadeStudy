package com.danielculpian.cascadestudy.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danielculpian.cascadestudy.domain.TimerState
import com.danielculpian.cascadestudy.presentation.components.AuthorCredits
import com.danielculpian.cascadestudy.presentation.components.StudyStatisticsCard

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

@Composable
private fun stateBadgeColors(state: TimerState): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()
    return when (state) {
        TimerState.STUDYING -> {
            if (isDark) {
                Color(0xFF1E3A2B) to Color(0xFFA3E6C3)
            } else {
                Color(0xFFE8F5E9) to Color(0xFF1B5E20)
            }
        }
        TimerState.RESTING -> {
            if (isDark) {
                Color(0xFF3E1E22) to Color(0xFFF88B8B)
            } else {
                Color(0xFFFFEBEE) to Color(0xFFB71C1C)
            }
        }
        else -> {
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
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
        StudyStatisticsCard(
            totalHistoricalHours = uiState.totalHistoricalHours,
            weeklyHours = uiState.weeklyHours,
            todayHours = uiState.todayHours
        )

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
                val (badgeContainerColor, badgeContentColor) = stateBadgeColors(uiState.state)

                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeContainerColor
                ) {
                    Text(
                        text = stateText(uiState.state),
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        color = badgeContentColor,
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

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        AuthorCredits()
    }
}
