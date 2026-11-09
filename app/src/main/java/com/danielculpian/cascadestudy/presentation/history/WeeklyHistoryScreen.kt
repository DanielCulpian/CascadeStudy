package com.danielculpian.cascadestudy.presentation.history

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.danielculpian.cascadestudy.domain.WeeklySummary
import com.danielculpian.cascadestudy.presentation.components.AuthorCredits
import kotlin.math.roundToInt

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

private fun formatDisplayTime(hours: Float): String {
    val totalSeconds = (hours * 3600f).toLong()
    if (totalSeconds <= 0L) return "0.0 h"

    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60

    return if (h > 0) {
        if (m > 0) "${h}h ${m}m" else "${h}h"
    } else {
        "${m}m"
    }
}

@Composable
fun WeeklyHistoryScreen(
    uiState: WeeklyHistoryUiState,
    onBackClick: () -> Unit,
    onSelectYear: (Int?) -> Unit,
    onSelectMonth: (Int?) -> Unit,
    onSetHourFilter: (HourFilterType, Float) -> Unit,
    onResetFilters: () -> Unit
) {
    var showYearDropdown by remember { mutableStateOf(false) }
    var showMonthDropdown by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(uiState.filterHoursThreshold) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Historial Semanal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.allSummaries.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box {
                            FilterChip(
                                selected = uiState.selectedYear != null,
                                onClick = { showYearDropdown = true },
                                label = {
                                    Text(
                                        if (uiState.selectedYear != null) "Año: ${uiState.selectedYear}" else "Año: Todos"
                                    )
                                }
                            )
                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos") },
                                    onClick = {
                                        onSelectYear(null)
                                        showYearDropdown = false
                                    }
                                )
                                uiState.availableYears.forEach { year ->
                                    DropdownMenuItem(
                                        text = { Text(year.toString()) },
                                        onClick = {
                                            onSelectYear(year)
                                            showYearDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Box {
                            FilterChip(
                                selected = uiState.selectedMonth != null,
                                onClick = { showMonthDropdown = true },
                                label = {
                                    Text(
                                        if (uiState.selectedMonth != null) "Mes: ${MONTH_NAMES[uiState.selectedMonth - 1]}" else "Mes: Todos"
                                    )
                                }
                            )
                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos") },
                                    onClick = {
                                        onSelectMonth(null)
                                        showMonthDropdown = false
                                    }
                                )
                                MONTH_NAMES.forEachIndexed { index, monthName ->
                                    DropdownMenuItem(
                                        text = { Text(monthName) },
                                        onClick = {
                                            onSelectMonth(index + 1)
                                            showMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.hourFilterType == HourFilterType.ALL,
                            onClick = { onSetHourFilter(HourFilterType.ALL, sliderValue) },
                            label = { Text("Todas") }
                        )
                        FilterChip(
                            selected = uiState.hourFilterType == HourFilterType.MORE_THAN,
                            onClick = { onSetHourFilter(HourFilterType.MORE_THAN, sliderValue) },
                            label = { Text("≥ ${sliderValue.roundToInt()}h") }
                        )
                        FilterChip(
                            selected = uiState.hourFilterType == HourFilterType.LESS_THAN,
                            onClick = { onSetHourFilter(HourFilterType.LESS_THAN, sliderValue) },
                            label = { Text("≤ ${sliderValue.roundToInt()}h") }
                        )
                    }

                    if (uiState.hourFilterType != HourFilterType.ALL) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Filtro de horas: ${sliderValue.roundToInt()} h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                sliderValue = it
                                onSetHourFilter(uiState.hourFilterType, sliderValue)
                            },
                            valueRange = 0f..40f,
                            steps = 39
                        )
                    }

                    if (uiState.selectedYear != null || uiState.selectedMonth != null || uiState.hourFilterType != HourFilterType.ALL) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                sliderValue = 0f
                                onResetFilters()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Limpiar filtros")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                uiState.allSummaries.isEmpty() -> {
                    EmptyStateCard(
                        title = "Aún no hay semanas registradas",
                        message = "Las semanas completadas (de lunes a domingo) se registrarán aquí automáticamente al finalizar cada semana."
                    )
                }

                uiState.filteredSummaries.isEmpty() -> {
                    EmptyStateCard(
                        title = "Sin resultados",
                        message = "No se encontraron semanas que coincidan con los filtros seleccionados."
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredSummaries,
                            key = { it.weekLabel }
                        ) { summary ->
                            WeeklySummaryCard(summary = summary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AuthorCredits()
        }
    }
}

@Composable
private fun WeeklySummaryCard(summary: WeeklySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = summary.weekLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Horas realizadas: ${formatDisplayTime(summary.totalHours)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (summary.totalSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (summary.totalSeconds > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${summary.totalHours.roundToInt()} h",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.totalSeconds > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
