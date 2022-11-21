package net.matsudamper.android.debugtool.screen.root

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import net.matsudamper.android.debugtool.adb.device.DeviceResult
import net.matsudamper.android.debugtool.compose.resources.AeroBlue
import net.matsudamper.android.debugtool.compose.resources.AnnotatedStringTag
import net.matsudamper.android.debugtool.compose.screen.root.RootUiState

class RootUiStateCreator(
    private val viewModelListener: Listener,
    private val uiStateListener: RootUiState.Listener
) {
    fun create(
        viewModelState: RootViewModel.ViewModelState,
        currentDevice: DeviceResult?,
    ): RootUiState {
        return RootUiState(
            minimalDrawer = viewModelState.minimalDrawer,
            currentPageType = viewModelState.pageType,
            errorDialog = viewModelState.error?.let { error ->
                when (error) {
                    is RootViewModel.ViewModelState.Error.AdbNotFound -> {
                        val title = buildAnnotatedString {
                            append("SDK Platform-Toolsがインストールされていません。")
                            append(
                                buildAnnotatedString {
                                    val displayUrl = "https://developer.android.com/studio/releases/platform-tools"
                                    append(displayUrl)
                                    addStringAnnotation(
                                        tag = AnnotatedStringTag.Url.tagName,
                                        annotation = "https://developer.android.com/studio/releases/platform-tools?hl=ja",
                                        start = 0,
                                        end = displayUrl.length,
                                    )
                                }
                            )
                            append("からダウンロードして、再度開いてください。")
                        }
                        RootUiState.ErrorDialog(
                            title = title,
                            errorText = buildString {
                                appendLine(error.e.message.orEmpty())
                                appendLine()
                                append(error.e.stackTraceToString())
                            },
                            onClickClose = {
                                viewModelListener.exitApplication()
                            },
                            onClickTitle = { index ->
                                val annotated = title.getStringAnnotations(index, index)
                                    .firstOrNull() ?: return@ErrorDialog

                                viewModelListener.openBrowser(annotated.item)
                            }
                        )
                    }

                    is RootViewModel.ViewModelState.Error.UnHandle -> {
                        RootUiState.ErrorDialog(
                            title = buildAnnotatedString {
                                append("未知のエラー")
                            },
                            errorText = buildString {
                                appendLine(error.e.message.orEmpty())
                                appendLine()
                                append(error.e.stackTraceToString())
                            },
                            onClickClose = {
                                viewModelListener.exitApplication()
                            },
                            onClickTitle = {},
                        )
                    }
                }
            },
            drawerItems = run {
                val activeColor = Color.AeroBlue
                listOf(
                    RootUiState.DrawerItem(
                        title = "Logs",
                        type = RootUiState.PageType.Logs,
                        color = activeColor.takeIf { viewModelState.pageType == RootUiState.PageType.Logs },
                        listener = object : RootUiState.DrawerItem.Listener {
                            override fun onClick() {
                                viewModelListener.onClickDrawer(
                                    pageType = RootUiState.PageType.Logs
                                )
                            }
                        },
                    ),
                    RootUiState.DrawerItem(
                        title = "images",
                        type = RootUiState.PageType.Images,
                        color = activeColor.takeIf { viewModelState.pageType == RootUiState.PageType.Images },
                        listener = object : RootUiState.DrawerItem.Listener {
                            override fun onClick() {
                                viewModelListener.onClickDrawer(
                                    pageType = RootUiState.PageType.Images
                                )
                            }
                        },
                    ),
                    RootUiState.DrawerItem(
                        title = "volumes",
                        type = RootUiState.PageType.Volume,
                        color = activeColor.takeIf { viewModelState.pageType == RootUiState.PageType.Volume },
                        listener = object : RootUiState.DrawerItem.Listener {
                            override fun onClick() {
                                viewModelListener.onClickDrawer(
                                    pageType = RootUiState.PageType.Volume
                                )
                            }
                        },
                    ),
                )
            },
            devices = viewModelState.devices.map { device ->
                RootUiState.Device(
                    name = device.name ?: device.device.state.name,
                    onClick = {
                        viewModelListener.onClickDevice(device)
                    }
                )
            },
            currentDevice = currentDevice?.name
                ?: currentDevice?.device?.state?.name.orEmpty(),
            listener = uiStateListener
        )
    }

    interface Listener {
        fun exitApplication()
        fun onClickDrawer(pageType: RootUiState.PageType)
        fun openBrowser(item: String)
        fun onClickDevice(device: DeviceResult)
    }
}