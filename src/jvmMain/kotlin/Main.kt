import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import net.matsudamper.android.debugtool.compose.screen.root.Root
import net.matsudamper.android.debugtool.screen.GlobalScope
import net.matsudamper.android.debugtool.screen.page.LogStreamUseCase
import net.matsudamper.android.debugtool.screen.page.LogsViewModel
import net.matsudamper.android.debugtool.screen.root.RootViewModel

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = application {
            Window(
                onCloseRequest = ::exitApplication,
                state = rememberWindowState(width = 1000.dp, height = 800.dp),
                title = "Android Debug Tools",
            ) {
                val coroutineScope = rememberCoroutineScope()

                val rootViewModel = remember(coroutineScope) {
                    RootViewModel(
                        coroutineScope = coroutineScope,
                        exitApplication = { exitApplication() },
                        adbCommandProcessor = GlobalScope.adbCommandProcessor,
                    )
                }
                val rootUiState = rootViewModel.uiState.collectAsState().value

                val logsViewModel = remember(coroutineScope) {
                    LogsViewModel(
                        coroutineScope = coroutineScope,
                        adbCommandProcessor = GlobalScope.adbCommandProcessor,
                        logStreamUseCase = LogStreamUseCase(
                            adbCommandProcessor = GlobalScope.adbCommandProcessor,
                        )
                    )
                }
                val logsUiState = logsViewModel.uiStateFlow.collectAsState().value
                Root(
                    rootUiState = rootUiState,
                    containersUiState = logsUiState,
                )
            }
        }
    }
}
