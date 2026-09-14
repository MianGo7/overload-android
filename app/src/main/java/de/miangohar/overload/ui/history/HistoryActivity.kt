package de.miangohar.overload.ui.history

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.miangohar.overload.R
import de.miangohar.overload.domain.logic.SetEntryCsvFormatter
import de.miangohar.overload.ui.theme.OverloadTheme
import java.io.File

/** Lists past training weeks and opens the detail screen for one of them. */
class HistoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OverloadTheme {
                val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                HistoryScreen(
                    state = state,
                    onTrendMuscleGroupSelected = viewModel::onTrendMuscleGroupSelected,
                    onOpenWeek = { week ->
                        val intent = Intent(this, WeekDetailActivity::class.java)
                            .putExtra(
                                WeekDetailActivity.EXTRA_WEEK_START_EPOCH_DAY,
                                week.startDate.toEpochDay(),
                            )
                        startActivity(intent)
                    },
                    onExportCsv = { exportCsv(SetEntryCsvFormatter.toCsv(state.entries)) },
                    onBack = { finish() },
                )
            }
        }
    }

    /**
     * Writes the log to the cache directory and shares it through a
     * [FileProvider] and an implicit `ACTION_SEND` intent, see ADR-0016.
     * `Uri`/`Intent`/`File` stay out of the view model on purpose, this is
     * the one place in the screen allowed to touch them.
     */
    private fun exportCsv(csv: String) {
        val file = File(cacheDir, "overload-log.csv")
        file.writeText(csv)
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.history_export_chooser_title)))
    }
}
