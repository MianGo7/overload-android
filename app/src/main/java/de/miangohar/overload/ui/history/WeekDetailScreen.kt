package de.miangohar.overload.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import de.miangohar.overload.R
import de.miangohar.overload.domain.model.SetEntry
import de.miangohar.overload.ui.common.MuscleVolumeCard
import de.miangohar.overload.ui.common.formatDate
import de.miangohar.overload.ui.common.formatKilograms
import de.miangohar.overload.ui.common.formatSets

/** Detailed breakdown of one past week. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekDetailScreen(
    state: WeekDetailUiState,
    onDeleteRequested: (Long) -> Unit,
    onBack: () -> Unit,
) {
    var entryPendingDeletion by remember { mutableStateOf<SetEntry?>(null) }

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
            val progress = state.progress
            if (progress == null || progress.muscleVolumes.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.week_detail_empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                item {
                    Text(
                        text = stringResource(
                            R.string.dashboard_total_sets,
                            formatSets(progress.totalSets),
                        ),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(progress.muscleVolumes, key = { "muscle-${it.muscleGroup.name}" }) { volume ->
                    MuscleVolumeCard(volume)
                }

                item {
                    Text(
                        text = stringResource(R.string.week_detail_entries_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(state.entries, key = { "entry-${it.id}" }) { entry ->
                    LoggedEntryCard(entry = entry, onDeleteRequested = { entryPendingDeletion = entry })
                }
            }
        }
    }

    entryPendingDeletion?.let { entry ->
        DeleteConfirmationDialog(
            entry = entry,
            onConfirm = {
                onDeleteRequested(entry.id)
                entryPendingDeletion = null
            },
            onDismiss = { entryPendingDeletion = null },
        )
    }
}

@Composable
private fun LoggedEntryCard(entry: SetEntry, onDeleteRequested: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = entry.exerciseName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(
                        R.string.week_detail_entry_summary,
                        entry.sets,
                        entry.reps,
                        formatKilograms(entry.weightKg),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(text = formatDate(entry.date), style = MaterialTheme.typography.bodySmall)
            }

            val deleteDescription = stringResource(R.string.week_detail_delete_entry, entry.exerciseName)
            // TextButton already merges its own semantics, so an outer
            // contentDescription does not reach it. clearAndSetSemantics
            // replaces its whole subtree, so the click action it would
            // otherwise carry has to be restated here too, see GoalScreen's
            // block length stepper for the same pattern.
            TextButton(
                onClick = onDeleteRequested,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = deleteDescription
                    role = Role.Button
                    onClick(label = null) { onDeleteRequested(); true }
                },
            ) {
                Text(stringResource(R.string.action_delete))
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    entry: SetEntry,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.week_detail_delete_confirm_title)) },
        text = { Text(stringResource(R.string.week_detail_delete_confirm_message, entry.exerciseName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
