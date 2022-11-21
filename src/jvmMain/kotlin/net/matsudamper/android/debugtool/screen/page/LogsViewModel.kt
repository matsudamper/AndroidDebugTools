package net.matsudamper.android.debugtool.screen.page

import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.matsudamper.android.debugtool.adb.AdbCommandProcessor
import net.matsudamper.android.debugtool.adb.log.AdbLogResult
import net.matsudamper.android.debugtool.compose.screen.page.containers.LogsPageUiState
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField


class LogsViewModel(
    private val coroutineScope: CoroutineScope,
    private val adbCommandProcessor: AdbCommandProcessor,
    private val logStreamUseCase: LogStreamUseCase,
) {
    private val viewModelStateFlow: MutableStateFlow<ViewModelState> = MutableStateFlow(ViewModelState())
    val uiStateFlow: StateFlow<LogsPageUiState> = MutableStateFlow(
        LogsPageUiState(
            logs = listOf(),
            order = listOf(
                LogsPageUiState.Order.Id,
                LogsPageUiState.Order.Image,
                LogsPageUiState.Order.Date,
                LogsPageUiState.Order.Names,
            ),
            filterText = TextFieldValue(),
            filterSuggests = listOf(),
            listener = object : LogsPageUiState.Listener {
                override fun onResume() {

                }

                override fun onFilterTextChanged(textFieldValue: TextFieldValue) {
                    viewModelStateFlow.update { viewModelState ->
                        viewModelState.copy(
                            textFieldValue = textFieldValue,
                            escapePressTextState = viewModelState.escapePressTextState
                                .takeIf { it == textFieldValue.text },
                        )
                    }
                }

                override fun onPressSuggestEscape() {
                    viewModelStateFlow.update {
                        it.copy(
                            escapePressTextState = it.textFieldValue.text
                        )
                    }
                }
            }
        )
    ).also { mutableStateFlow ->
        coroutineScope.launch {
            val dateTimeFormat = DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter()
            viewModelStateFlow.buffer(1, BufferOverflow.DROP_OLDEST).collect { viewModelState ->
                mutableStateFlow.update {
                    it.copy(
                        logs = viewModelState.logs
                            .filter { logResult ->
                                sequence {
                                    yield(
                                        viewModelState.logFilters.filterIsInstance<LogFilterParser.Result.FreeText>()
                                            .map { result -> result.text }
                                            .all { freeText -> logResult.body.contains(freeText) }
                                    )
                                    yield(
                                        run {
                                            val packageFilters = viewModelState.logFilters
                                                .filterIsInstance<LogFilterParser.Result.KeyValue>()
                                                .filter { result -> result.key == LogFilterKey.Package }
                                            if (packageFilters.isEmpty()) {
                                                true
                                            } else {
                                                packageFilters.any { (_, value) -> logResult.pid.toString() == value || viewModelState.pidToNames[logResult.pid] == value }
                                            }
                                        }
                                    )
                                    yield(
                                        run {
                                            val tagFilters = viewModelState.logFilters
                                                .filterIsInstance<LogFilterParser.Result.KeyValue>()
                                                .filter { result -> result.key == LogFilterKey.Tag }
                                            if (tagFilters.isEmpty()) {
                                                true
                                            } else {
                                                tagFilters.any { (_, value) -> logResult.tag == value }
                                            }
                                        }
                                    )
                                }.all { filter -> filter }
                            }
                            .map { log ->
                                LogsPageUiState.Log(
                                    key = log.index,
                                    body = log.body.trimEnd(),
                                    time = log.dateTime.format(dateTimeFormat),
                                    pidOrName = viewModelState.pidToNames[log.pid] ?: log.pid.toString(),
                                )
                            }.reversed(),
                    )
                }
            }
        }
        coroutineScope.launch {
            viewModelStateFlow.collect { viewModelState ->
                val suggests by lazy {
                    viewModelState.filterSuggests.map { suggestItem ->
                        LogsPageUiState.FilterSuggest(
                            text = suggestItem.suggestText
                        )
                    }
                }

                mutableStateFlow.update {
                    it.copy(
                        filterText = viewModelState.textFieldValue,
                        filterSuggests = if (viewModelState.textFieldValue.text == viewModelState.escapePressTextState) {
                            listOf()
                        } else {
                            suggests
                        },
                    )
                }
            }
        }
    }

    init {
        coroutineScope.launch {
            logStreamUseCase.logStateFlow.collect { logLines ->
                viewModelStateFlow.update { viewModelState ->
                    viewModelState.copy(
                        logs = logLines,
                    )
                }
            }
        }
        coroutineScope.launch {
            adbCommandProcessor.currentDeviceFlow.filterNotNull().collect {
                coroutineScope {
                    while (isActive) {
                        val process = adbCommandProcessor.getProcess()
                        viewModelStateFlow.update { viewModelState ->
                            viewModelState.copy(
                                pidToNames = process.associate { it.pid to it.name },
                            )
                        }
                        delay(15 * 1000)
                    }
                }
            }
        }
        coroutineScope.launch {
            val getLogFilterSuggestUseCase = GetLogFilterSuggestUseCase()
            val parser = LogFilterParser()
            viewModelStateFlow
                .map { it.textFieldValue }
                .buffer(1, BufferOverflow.DROP_OLDEST)
                .collect { filterTextState ->
                    val logFilters = parser.parse(filterTextState.text)
                    val suggests = getLogFilterSuggestUseCase.exec(
                        logFilters = logFilters,
                        cursorIndex = filterTextState.selection.end,
                    )
                    viewModelStateFlow.update {
                        it.copy(
                            logFilters = logFilters,
                            filterSuggests = suggests,
                        )
                    }
                }
        }
        coroutineScope.launch {
            logStreamUseCase.connect()
        }
    }

    private data class ViewModelState(
        val logs: List<AdbLogResult> = listOf(),
        val pidToNames: Map<Int, String> = mapOf(),
        val logStreamEnabled: Boolean = false,
        val textFieldValue: TextFieldValue = TextFieldValue(),
        val escapePressTextState: String? = null,
        val filterSuggests: List<GetLogFilterSuggestUseCase.SuggestItem> = listOf(),
        val logFilters: List<LogFilterParser.Result> = listOf(),
    )
}
