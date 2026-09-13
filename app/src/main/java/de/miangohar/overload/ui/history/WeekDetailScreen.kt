package de.miangohar.overload.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import de.miangohar.overload.ui.common.MuscleVolumeCard
import de.miangohar.overload.ui.common.formatDate
import de.miangohar.overload.ui.common.formatSets

/** Detailed breakdown of one past week. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekDetailScreen(
    state: WeekDetailUiState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            R.string.dashboard_week_range,
                            formatDate(state.week.startDate),
                            formatDate(state.week.endDate),
                        ),
                    )
                },
                actions = {
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
            state.progress?.let { progress ->
                item {
                    Text(
                        text = stringResource(
                            R.string.dashboard_total_sets,
                            formatSets(progress.totalSets),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(progress.muscleVolumes, key = { it.muscleGroup.name }) { volume ->
                    MuscleVolumeCard(volume)
                }
            }
        }
    }
}
