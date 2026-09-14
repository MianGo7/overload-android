package de.miangohar.overload.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import de.miangohar.overload.domain.model.WeeklyTotal

/**
 * A weekly volume trend, a line connecting one point per week with the
 * target range drawn as a band behind it. Plain lines and fills rather than
 * a charting library, see ADR-0015.
 *
 * The canvas itself carries no semantics, [summary] is what a screen reader
 * announces instead, the chart is decorative alongside it.
 */
@Composable
fun VolumeTrendChart(
    trend: List<WeeklyTotal>,
    targetBand: ClosedFloatingPointRange<Double>?,
    summary: String,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val bandColor = MaterialTheme.colorScheme.primaryContainer
    val axisColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clearAndSetSemantics {},
        ) {
            val values = trend.map { it.totalSets }
            val upperBound = maxOf(values.maxOrNull() ?: 0.0, targetBand?.endInclusive ?: 0.0, 1.0) * 1.1
            fun yOf(value: Double): Float = (size.height * (1 - (value / upperBound))).toFloat()

            drawLine(
                color = axisColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx(),
            )

            if (targetBand != null) {
                val top = yOf(targetBand.endInclusive)
                val bottom = yOf(targetBand.start)
                drawRect(
                    color = bandColor,
                    topLeft = Offset(0f, top),
                    size = Size(size.width, bottom - top),
                    alpha = 0.5f,
                )
            }

            if (trend.size > 1) {
                val xStep = size.width / (trend.size - 1)
                val points = values.mapIndexed { index, value -> Offset(index * xStep, yOf(value)) }
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                points.forEach { point ->
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point)
                }
            } else if (trend.size == 1) {
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(size.width / 2f, yOf(values.first())),
                )
            }
        }
        Text(text = summary, style = MaterialTheme.typography.bodyMedium)
    }
}
