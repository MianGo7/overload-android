package de.miangohar.overload.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingWeek
import de.miangohar.overload.ui.common.formatDate
import de.miangohar.overload.ui.common.formatSets
import de.miangohar.overload.ui.common.label

/**
 * Past weeks with their total volume, newest first.
 *
 * `Scaffold`, `TopAppBar` and `FilterChip` are still marked experimental in
 * Material 3, and `FlowRow` is still experimental in Compose's layout
 * package, neither has a stable equivalent yet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onTrendMuscleGroupSelected: (MuscleGroup?) -> Unit,
    onOpenWeek: (TrainingWeek) -> Unit,
    onExportCsv: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                actions = {
                    TextButton(onClick = onExportCsv) {
                        Text(stringResource(R.string.history_export_action))
                    }
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.isLoading) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.history_trend_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            FilterChip(
                                selected = state.selectedMuscleGroup == null,
                                onClick = { onTrendMuscleGroupSelected(null) },
                                label = { Text(stringResource(R.string.history_trend_overall)) },
                            )
                            MuscleGroup.entries.forEach { muscleGroup ->
                                FilterChip(
                                    selected = state.selectedMuscleGroup == muscleGroup,
                                    onClick = { onTrendMuscleGroupSelected(muscleGroup) },
                                    label = { Text(muscleGroup.label()) },
                                )
                            }
                        }
                        VolumeTrendChart(
                            trend = state.trend,
                            targetBand = state.targetBand,
                            summary = trendSummary(state),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            if (state.rows.isEmpty() && !state.isLoading) {
                item {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            items(state.rows, key = { it.week.startDate.toEpochDay() }) { row ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWeek(row.week) },
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.history_week,
                                formatDate(row.week.startDate),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stringResource(
                                R.string.volume_sets_plain,
                                formatSets(row.totalSets),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (row.isDeload) {
                            Text(
                                text = stringResource(R.string.history_deload),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** The current week's total against the target band, for screen readers. */
@Composable
private fun trendSummary(state: HistoryUiState): String {
    val currentWeekSets = state.trend.lastOrNull()?.totalSets ?: 0.0
    val band = state.targetBand
    return if (band != null) {
        stringResource(
            R.string.history_trend_summary_ranged,
            formatSets(currentWeekSets),
            band.start.toInt(),
            band.endInclusive.toInt(),
        )
    } else {
        stringResource(R.string.history_trend_summary_plain, formatSets(currentWeekSets))
    }
}
