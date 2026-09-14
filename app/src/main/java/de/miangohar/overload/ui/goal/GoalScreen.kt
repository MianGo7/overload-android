package de.miangohar.overload.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.domain.model.TrainingGoal
import de.miangohar.overload.domain.model.TrainingPhase
import de.miangohar.overload.ui.common.label

/** Form for the individual goal: phase, block length and weekly set ranges. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    state: GoalUiState,
    onPhaseSelected: (TrainingPhase) -> Unit,
    onBlockLengthChanged: (Int) -> Unit,
    onMinChanged: (MuscleGroup, String) -> Unit,
    onMaxChanged: (MuscleGroup, String) -> Unit,
    onPrefillRequested: () -> Unit,
    onSaveRequested: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.goal_title)) },
                actions = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.goal_phase),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TrainingPhase.entries.forEach { phase ->
                    FilterChip(
                        selected = state.phase == phase,
                        onClick = { onPhaseSelected(phase) },
                        label = { Text(phase.label()) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.goal_block_length),
                style = MaterialTheme.typography.titleMedium,
            )
            val decreaseDescription = stringResource(R.string.goal_block_length_decrease)
            val increaseDescription = stringResource(R.string.goal_block_length_increase)
            val canDecrease = state.blockLengthWeeks > TrainingGoal.MIN_BLOCK_LENGTH_WEEKS
            val canIncrease = state.blockLengthWeeks < TrainingGoal.MAX_BLOCK_LENGTH_WEEKS
            val decrease = { onBlockLengthChanged(state.blockLengthWeeks - 1) }
            val increase = { onBlockLengthChanged(state.blockLengthWeeks + 1) }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // OutlinedButton already merges its own semantics, so an outer
                // contentDescription does not reach it. clearAndSetSemantics
                // replaces its whole subtree, so the click action it would
                // otherwise carry has to be restated here too.
                OutlinedButton(
                    onClick = decrease,
                    enabled = canDecrease,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = decreaseDescription
                        role = Role.Button
                        if (canDecrease) onClick(label = null) { decrease(); true } else disabled()
                    },
                ) {
                    Text("-")
                }
                Text(
                    text = state.blockLengthWeeks.toString(),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedButton(
                    onClick = increase,
                    enabled = canIncrease,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = increaseDescription
                        role = Role.Button
                        if (canIncrease) onClick(label = null) { increase(); true } else disabled()
                    },
                ) {
                    Text("+")
                }
            }

            Text(
                text = stringResource(R.string.goal_targets),
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(onClick = onPrefillRequested) {
                Text(stringResource(R.string.goal_action_prefill))
            }

            state.rows.forEach { row ->
                // A field's height grows if its label wraps, so the row is
                // stretched to its tallest member rather than left uneven.
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = row.muscleGroup.label(),
                        modifier = Modifier.width(120.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    OutlinedTextField(
                        value = row.minSets,
                        onValueChange = { onMinChanged(row.muscleGroup, it) },
                        label = { Text(stringResource(R.string.goal_min)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(96.dp).fillMaxHeight(),
                    )
                    OutlinedTextField(
                        value = row.maxSets,
                        onValueChange = { onMaxChanged(row.muscleGroup, it) },
                        label = { Text(stringResource(R.string.goal_max)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(96.dp).fillMaxHeight(),
                    )
                }
            }

            state.errorRes?.let { errorRes ->
                Text(
                    text = stringResource(errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = onSaveRequested,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.goal_action_save))
            }
        }
    }
}
