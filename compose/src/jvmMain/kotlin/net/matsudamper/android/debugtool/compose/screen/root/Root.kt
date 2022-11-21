package net.matsudamper.android.debugtool.compose.screen.root

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import net.matsudamper.android.debugtool.compose.layout.CustomScaffold
import net.matsudamper.android.debugtool.compose.resources.AliceBlue
import net.matsudamper.android.debugtool.compose.screen.drawer.DrawerContent
import net.matsudamper.android.debugtool.compose.screen.page.containers.LogsPage
import net.matsudamper.android.debugtool.compose.screen.page.images.ImagesPage
import net.matsudamper.android.debugtool.compose.resources.AppTheme
import net.matsudamper.android.debugtool.compose.resources.applyStyle
import net.matsudamper.android.debugtool.compose.screen.page.containers.LogsPageUiState
import java.util.UUID

@Composable
fun Root(
    rootUiState: RootUiState,
    containersUiState: LogsPageUiState,
) {
    if (rootUiState.errorDialog != null) {
        ErrorDialog(
            errorDialog = rootUiState.errorDialog,
        )
    }

    AppTheme {
        CustomScaffold(
            modifier = Modifier.fillMaxSize(),
            drawerContent = {
                Surface(color = Color.AliceBlue) {
                    DrawerContent(
                        modifier = Modifier.fillMaxHeight(),
                        minimalDrawer = rootUiState.minimalDrawer,
                        onClickMenuExpand = { rootUiState.listener.onClickMenuExpand() },
                        items = rootUiState.drawerItems,
                    )
                }
            },
            topAppBar = {
                AdbTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    currentDevice = rootUiState.currentDevice,
                    devices = rootUiState.devices,
                    onClickRefresh = { rootUiState.listener.onClickRefresh() }
                )
            }
        ) {
            val modifier = Modifier.fillMaxSize()
            when (rootUiState.currentPageType) {
                RootUiState.PageType.Images -> {
                    ImagesPage(modifier = modifier)
                }

                RootUiState.PageType.Logs -> {
                    LogsPage(
                        modifier = modifier,
                        uiState = containersUiState,
                    )
                }

                RootUiState.PageType.Volume -> {
                    Text("TODO")
                }
            }
        }
    }
}

@Composable
private fun ErrorDialog(
    errorDialog: RootUiState.ErrorDialog,
) {
    Dialog(
        onCloseRequest = {},
        state = rememberDialogState(size = DpSize(800.dp, 600.dp)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(12.dp)
        ) {
            val colors = MaterialTheme.colorScheme
            ClickableText(
                modifier = Modifier.fillMaxWidth(),
                text = remember(errorDialog.title) {
                    errorDialog.title.applyStyle(colors)
                },
                onClick = { index ->
                    errorDialog.onClickTitle(index)
                },
            )
            BasicTextField(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
                    .background(color = Color.LightGray)
                    .padding(8.dp),
                value = errorDialog.errorText,
                onValueChange = {},
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(Modifier.weight(1f))
                Button(onClick = {
                    errorDialog.onClickClose()
                }) {
                    Text(text = "Close")
                }
            }
        }
    }
}

@Composable
private fun AdbTopAppBar(
    modifier: Modifier,
    currentDevice: String,
    devices: List<RootUiState.Device>,
    onClickRefresh: () -> Unit,
) {
    Surface(
        modifier = modifier.height(38.dp),
        color = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                var expanded: Boolean by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.width(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                            .clickable(expanded.not()) { expanded = expanded.not() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(currentDevice)
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Choose Device"
                        )
                    }
                    Surface(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                        DropdownMenu(
                            modifier = Modifier.width(200.dp),
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            devices.map { device ->
                                DropdownMenuItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        device.onClick()
                                        expanded = false
                                    }
                                ) {
                                    Text(
                                        text = device.name,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = {
                    onClickRefresh()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "device refresh",
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun Preview() {
    Root(
        rootUiState = RootUiState(
            minimalDrawer = false,
            currentPageType = RootUiState.PageType.Logs,
            drawerItems = listOf(
                RootUiState.DrawerItem(
                    title = "title",
                    type = RootUiState.PageType.Logs,
                    color = Color.AliceBlue,
                    listener = object : RootUiState.DrawerItem.Listener {
                        override fun onClick() {}
                    },
                )
            ),
            devices = listOf(),
            errorDialog = null,
            currentDevice = "Pixel8",
            listener = object : RootUiState.Listener {
                override fun onClickMenuExpand() {}
                override fun onClickRefresh() {}
            },
        ),
        containersUiState = LogsPageUiState(
            logs = (0..100).map {
                LogsPageUiState.Log(
                    key = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString(),
                    pidOrName = "",
                    time = "",
                )
            },
            order = listOf(
                LogsPageUiState.Order.Id,
                LogsPageUiState.Order.Names,
                LogsPageUiState.Order.Image,
                LogsPageUiState.Order.Date,
            ),
            filterText = TextFieldValue(),
            filterSuggests = listOf(),
            listener = object : LogsPageUiState.Listener {
                override fun onResume() {}
                override fun onFilterTextChanged(textFieldValue: TextFieldValue) {}
                override fun onPressSuggestEscape() {}
            },
        )
    )
}
