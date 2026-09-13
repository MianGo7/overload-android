package de.miangohar.overload.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
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
import de.miangohar.overload.domain.logic.DeloadReason
import de.miangohar.overload.domain.logic.DeloadRecommendation
import de.miangohar.overload.domain.model.WeeklyProgress
import de.miangohar.overload.ui.common.MuscleVolumeCard
import de.miangohar.overload.ui.common.formatDate
import de.miangohar.overload.ui.common.formatKilograms
import de.miangohar.overload.ui.common.formatSets
import kotlin.math.roundToInt

/**
 * The weekly overview. It answers one question at a glance: which muscle
 * groups are still short of their weekly set range.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onLogSets: () -> Unit,
    onEditGoal: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.dashboard_title))
                        Text(
                            text = stringResource(
                                R.string.dashboard_week_range,
                                formatDate(state.week.startDate),
                                formatDate(state.week.endDate),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onEditGoal) {
                        Text(stringResource(R.string.dashboard_action_goal))
                    }
                    TextButton(onClick = onOpenHistory) {
                        Text(stringResource(R.string.dashboard_action_history))
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onLogSets) {
                Text(stringResource(R.string.dashboard_action_log))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.progress?.let { progress ->
                item { WeekSummaryCard(progress) }
            }

            state.deload?.let { recommendation ->
                item { DeloadCard(recommendation, state.blockLengthWeeks) }
            }

            if (!state.hasGoal && !state.isLoading) {
                item { InfoCard(stringResource(R.string.dashboard_empty_goal)) }
            }

            val volumes = state.progress?.muscleVolumes.orEmpty()
            if (volumes.isEmpty() && !state.isLoading) {
                item { InfoCard(stringResource(R.string.dashboard_empty_entries)) }
            }

            items(volumes, key = { it.muscleGroup.name }) { volume ->
                MuscleVolumeCard(volume)
            }
        }
    }
}

@Composable
private fun WeekSummaryCard(progress: WeeklyProgress) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.dashboard_overall_progress,
                    (progress.overallCompletion * 100).roundToInt(),
                ),
                style = MaterialTheme.typography.titleLarge,
            )
            LinearProgressIndicator(
                progress = { progress.overallCompletion.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(
                    R.string.dashboard_total_sets,
                    formatSets(progress.totalSets),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(
                    R.string.dashboard_volume_load,
                    formatKilograms(progress.volumeLoadKg),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DeloadCard(recommendation: DeloadRecommendation, blockLengthWeeks: Int) {
    val message = when (recommendation.reason) {
        DeloadReason.NO_HISTORY -> null
        DeloadReason.DELOAD_IN_PROGRESS -> stringResource(R.string.deload_in_progress)
        DeloadReason.WITHIN_BLOCK -> stringResource(
            R.string.deload_within_block,
            recommendation.accumulationWeeks,
            blockLengthWeeks,
        )

        DeloadReason.BLOCK_COMPLETE -> stringResource(
            R.string.deload_due_body,
            recommendation.accumulationWeeks,
        )
    } ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (recommendation.isDue) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (recommendation.isDue) {
                Text(
                    text = stringResource(R.string.deload_due_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InfoCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
