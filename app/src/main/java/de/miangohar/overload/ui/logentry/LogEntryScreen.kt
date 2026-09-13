package de.miangohar.overload.ui.logentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleGroup
import de.miangohar.overload.ui.common.formatDate
import de.miangohar.overload.ui.common.label

/** Entry form for one block of straight sets. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogEntryScreen(
    state: LogEntryUiState,
    onExerciseNameChanged: (String) -> Unit,
    onPrimaryMuscleSelected: (MuscleGroup) -> Unit,
    onSecondaryMuscleToggled: (MuscleGroup) -> Unit,
    onSetsChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRirChanged: (String) -> Unit,
    onDateShifted: (Long) -> Unit,
    onSaveRequested: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.log_title)) },
                actions = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.log_date),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { onDateShifted(-1L) }) {
                    Text(stringResource(R.string.log_date_earlier))
                }
                Text(
                    text = formatDate(state.date),
                    style = MaterialTheme.typography.titleMedium,
                )
                OutlinedButton(onClick = { onDateShifted(1L) }) {
                    Text(stringResource(R.string.log_date_later))
                }
            }

            OutlinedTextField(
                value = state.exerciseName,
                onValueChange = onExerciseNameChanged,
                label = { Text(stringResource(R.string.log_exercise)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.log_primary_muscle),
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MuscleGroup.entries.forEach { muscleGroup ->
                    FilterChip(
                        selected = state.primaryMuscle == muscleGroup,
                        onClick = { onPrimaryMuscleSelected(muscleGroup) },
                        label = { Text(muscleGroup.label()) },
                    )
                }
            }

            Text(
                text = stringResource(R.string.log_secondary_muscles),
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MuscleGroup.entries
                    .filter { it != state.primaryMuscle }
                    .forEach { muscleGroup ->
                        FilterChip(
                            selected = muscleGroup in state.secondaryMuscles,
                            onClick = { onSecondaryMuscleToggled(muscleGroup) },
                            label = { Text(muscleGroup.label()) },
                        )
                    }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.sets,
                    onValueChange = onSetsChanged,
                    label = { Text(stringResource(R.string.log_sets)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.reps,
                    onValueChange = onRepsChanged,
                    label = { Text(stringResource(R.string.log_reps)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = state.weightKg,
                    onValueChange = onWeightChanged,
                    label = { Text(stringResource(R.string.log_weight)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = state.rir,
                    onValueChange = onRirChanged,
                    label = { Text(stringResource(R.string.log_rir)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
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
                Text(stringResource(R.string.log_action_save))
            }
        }
    }
}
