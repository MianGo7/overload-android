package de.miangohar.overload.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.miangohar.overload.R
import de.miangohar.overload.domain.model.MuscleVolume
import de.miangohar.overload.domain.model.VolumeStatus
import de.miangohar.overload.ui.theme.AboveTarget
import de.miangohar.overload.ui.theme.BelowTarget
import de.miangohar.overload.ui.theme.InTarget
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.floor

/** Formats fractional sets without a trailing zero, so 4.0 reads as 4. */
fun formatSets(sets: Double): String =
    if (sets == floor(sets)) {
        sets.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", sets)
    }

fun formatKilograms(value: Double): String =
    String.format(Locale.getDefault(), "%,.0f", value)

fun formatDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

@Composable
fun VolumeStatus.label(): String = stringResource(
    when (this) {
        VolumeStatus.UNTARGETED -> R.string.status_untargeted
        VolumeStatus.BELOW_TARGET -> R.string.status_below_target
        VolumeStatus.IN_TARGET -> R.string.status_in_target
        VolumeStatus.ABOVE_TARGET -> R.string.status_above_target
    },
)

fun VolumeStatus.color(): Color = when (this) {
    VolumeStatus.UNTARGETED -> Color.Gray
    VolumeStatus.BELOW_TARGET -> BelowTarget
    VolumeStatus.IN_TARGET -> InTarget
    VolumeStatus.ABOVE_TARGET -> AboveTarget
}

/**
 * One muscle group with its accumulated sets, its target range and a progress
 * bar towards the lower bound of that range.
 */
@Composable
fun MuscleVolumeCard(
    volume: MuscleVolume,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = volume.muscleGroup.label(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = volume.status.label(),
                    style = MaterialTheme.typography.labelLarge,
                    color = volume.status.color(),
                )
            }

            val target = volume.target
            Text(
                text = if (target == null) {
                    stringResource(R.string.volume_sets_plain, formatSets(volume.completedSets))
                } else {
                    stringResource(
                        R.string.volume_sets_of_range,
                        formatSets(volume.completedSets),
                        target.minSets,
                        target.maxSets,
                    )
                },
                style = MaterialTheme.typography.bodyMedium,
            )

            if (target != null) {
                LinearProgressIndicator(
                    progress = { volume.completionRatio.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
