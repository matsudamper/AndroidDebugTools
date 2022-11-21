package net.matsudamper.android.debugtool.screen.root

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.matsudamper.android.debugtool.adb.AdbCommandProcessor
import net.matsudamper.android.debugtool.adb.AdbException
import net.matsudamper.android.debugtool.adb.device.DeviceResult
import net.matsudamper.android.debugtool.compose.screen.root.RootUiState
import java.awt.Desktop
import java.net.URI

class RootViewModel(
    private val coroutineScope: CoroutineScope,
    private val exitApplication: () -> Unit,
    private val adbCommandProcessor: AdbCommandProcessor,
) {
    private val viewModelStateFlow = MutableStateFlow(ViewModelState())
    private val listener = object : RootUiState.Listener {
        override fun onClickMenuExpand() {
            viewModelStateFlow.update {
                it.copy(
                    minimalDrawer = it.minimalDrawer.not(),
                )
            }
        }

        override fun onClickRefresh() {
            coroutineScope.launch {
                updateDevice()
            }
        }
    }
    private val rootUiStateCreator = RootUiStateCreator(
        viewModelListener = object : RootUiStateCreator.Listener {
            override fun onClickDrawer(pageType: RootUiState.PageType) {
                viewModelStateFlow.update { viewModelState ->
                    viewModelState.copy(
                        pageType = pageType
                    )
                }
            }

            override fun exitApplication() {
                this@RootViewModel.exitApplication()
            }

            override fun openBrowser(item: String) {
                coroutineScope.launch(Dispatchers.IO) {
                    Desktop.getDesktop().browse(URI(item))
                }
            }

            override fun onClickDevice(device: DeviceResult) {
                adbCommandProcessor.setTargetDevice(device)
            }
        },
        uiStateListener = listener
    )
    val uiState: StateFlow<RootUiState> = MutableStateFlow(
        RootUiState(
            minimalDrawer = false,
            listener = listener,
            currentPageType = RootUiState.PageType.Logs,
            drawerItems = listOf(),
            devices = listOf(),
            errorDialog = null,
            currentDevice = "",
        )
    ).also { mutableStateFlow ->
        coroutineScope.launch {
            viewModelStateFlow.combine(adbCommandProcessor.currentDeviceFlow) { viewModelState, currentDevice ->
                mutableStateFlow.update {
                    rootUiStateCreator.create(
                        viewModelState = viewModelState,
                        currentDevice = currentDevice,
                    )
                }
            }.collect()
        }
    }

    init {
        coroutineScope.launch {
            val error = AdbCommandProcessor.startServer()
            println(error)
            if (error != null) {
                viewModelStateFlow.update {
                    it.copy(
                        error = ViewModelState.Error.AdbNotFound(error),
                    )
                }
            } else {
                updateDevice()
            }
        }
        coroutineScope.launch {
            adbCommandProcessor.devices.combine(adbCommandProcessor.currentDeviceFlow) { devices, oldDevice ->

                adbCommandProcessor.setTargetDevice(run {
                    oldDevice
                        ?.takeIf { it.device.serial in devices.map { device -> device.device.serial } }
                        ?: devices.firstOrNull()
                })

                viewModelStateFlow.update { viewModelState ->
                    viewModelState.copy(
                        devices = devices,
                    )
                }
            }.collect()
        }
    }

    private suspend fun updateDevice() {
        runCatching {
            adbCommandProcessor.updateDeviceList()
        }.onFailure {
            when (it as AdbException) {
                is AdbException.AdbNotConnected,
                is AdbException.AdbNotFound -> {
                    viewModelStateFlow.update { viewModelState ->
                        viewModelState.copy(
                            error = ViewModelState.Error.UnHandle(it)
                        )
                    }
                }

                is AdbException.UnknownFailure -> {
                    viewModelStateFlow.update { viewModelState ->
                        viewModelState.copy(
                            error = ViewModelState.Error.UnHandle(it)
                        )
                    }
                }
            }
        }.getOrNull() ?: return
    }

    data class ViewModelState(
        val minimalDrawer: Boolean = false,
        val pageType: RootUiState.PageType = RootUiState.PageType.Logs,
        val devices: List<DeviceResult> = listOf(),
        val error: Error? = null,
    ) {
        sealed interface Error {
            class AdbNotFound(
                val e: AdbException.AdbNotFound,
            ) : Error

            class UnHandle(
                val e: Throwable
            ) : Error
        }
    }
}
