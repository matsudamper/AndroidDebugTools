package net.matsudamper.android.debugtool.compose.screen.page.containers

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.matsudamper.android.debugtool.compose.resources.MyIcons

@Composable
fun LogsPage(
    modifier: Modifier,
    uiState: LogsPageUiState,
) {
    LaunchedEffect(uiState.listener) {
        uiState.listener.onResume()
    }

    Surface(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            var fitToTop by remember { mutableStateOf(true) }
            Header(
                modifier = Modifier.fillMaxWidth(),
                fitToTopEnabled = fitToTop,
                onClickFitToTop = { fitToTop = fitToTop.not() },
                onPressSuggestEscape = { uiState.listener.onPressSuggestEscape() },
                onFilterTextChanged = { newText -> uiState.listener.onFilterTextChanged(newText) },
                filterTextField = uiState.filterText,
                filterSuggest = uiState.filterSuggests,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val lazyColumState = rememberLazyListState()
                LaunchedEffect(lazyColumState.firstVisibleItemScrollOffset, lazyColumState.firstVisibleItemIndex) {
                    if (lazyColumState.firstVisibleItemIndex != 0 || lazyColumState.firstVisibleItemScrollOffset != 0) {
                        fitToTop = false
                    }
                }
                LaunchedEffect(fitToTop, uiState.logs) {
                    if (fitToTop) {
                        lazyColumState.scrollToItem(0)
                    }
                }
                LazyColumContent(
                    modifier = Modifier
                        .weight(1f),
                    logs = uiState.logs,
                    lazyColumState = lazyColumState,
                )
                VerticalScrollbar(
                    modifier = Modifier.fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(lazyColumState),
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Header(
    modifier: Modifier,
    fitToTopEnabled: Boolean,
    onClickFitToTop: () -> Unit,
    onPressSuggestEscape: () -> Unit,
    onFilterTextChanged: (TextFieldValue) -> Unit,
    filterTextField: TextFieldValue,
    filterSuggest: List<LogsPageUiState.FilterSuggest>,
) {
    Surface(
        modifier = modifier,
        elevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            ToggleButton(
                modifier = Modifier.size(32.dp),
                onClick = { onClickFitToTop() },
                painter = MyIcons.VerticalAlignTop,
                contentDescription = "Fit to Top",
                enabled = fitToTopEnabled,
            )
            Spacer(Modifier.weight(1f))

            var textFieldHasFocus by remember { mutableStateOf(false) }
            var menuCursorIndex by remember { mutableStateOf(0) }
            LaunchedEffect(menuCursorIndex, filterSuggest) {
                menuCursorIndex = when {
                    menuCursorIndex > filterSuggest.size - 1 -> 0
                    menuCursorIndex < 0 -> filterSuggest.size - 1
                    else -> return@LaunchedEffect
                }
            }

            Box {
                BasicTextField(
                    modifier = Modifier
                        .widthIn(min = 300.dp)
                        .onFocusEvent {
                            textFieldHasFocus = it.hasFocus
                        }
                        .onPreviewKeyEvent {
                            if (filterSuggest.isEmpty()) return@onPreviewKeyEvent false
                            when (it.type) {
                                KeyEventType.KeyDown -> Unit
                                else -> return@onPreviewKeyEvent false
                            }
                            when (it.key) {
                                Key.DirectionUp -> {
                                    menuCursorIndex -= 1
                                    true
                                }

                                Key.DirectionDown -> {
                                    menuCursorIndex += 1
                                    true
                                }

                                Key.Escape -> {
                                    onPressSuggestEscape()
                                    true
                                }

                                else -> false
                            }
                        },
                    value = filterTextField,
                    maxLines = 1,
                    onValueChange = { newTextField ->
                        onFilterTextChanged(newTextField)
                    },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        SolidColor(
                                            if (textFieldHasFocus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        )
                                    ),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            innerTextField()
                        }
                    },
                )
                if (textFieldHasFocus && filterSuggest.isNotEmpty()) {
                    DropdownMenu(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        expanded = textFieldHasFocus,
                        focusable = false,
                        onDismissRequest = {},
                    ) {
                        filterSuggest.forEachIndexed { contentIndex, it ->
                            SuggestFilterContent(
                                modifier = Modifier
                                    .heightIn(max = 500.dp)
                                    .width(200.dp)
                                    .padding(8.dp),
                                onClick = {},
                                selected = contentIndex == menuCursorIndex
                            ) {
                                Text(
                                    text = it.text
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestFilterContent(
    modifier: Modifier,
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier.background(if (selected) LocalContentColor.current.copy(0.2f) else Color.Transparent)
            .clickable { onClick() }
            .then(modifier),
    ) {
        content()
    }
}

@Composable
@NonRestartableComposable
@Suppress("SameParameterValue")
private fun ToggleButton(
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
) {
    Surface(
        modifier = modifier.clickable(
            indication = rememberRipple(color = if (enabled) Color.White else Color.Black),
            interactionSource = remember { MutableInteractionSource() },
        ) { onClick() },
        shape = RoundedCornerShape(4.dp),
        color = if (enabled) Color.LightGray else Color.Transparent,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun LazyColumContent(
    modifier: Modifier,
    logs: List<LogsPageUiState.Log>,
    lazyColumState: LazyListState,
) {
    LazyColumn(
        modifier = modifier,
        state = lazyColumState
    ) {
        items(
            items = logs,
            key = { it.key },
        ) { item ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                val itemPadding = Modifier.padding(4.dp)
                Text(
                    modifier = Modifier.then(itemPadding),
                    text = item.time,
                    fontSize = 14.sp,
                )
                ListRowDivider()
                Text(
                    modifier = Modifier.then(itemPadding)
                        .width(100.dp),
                    text = item.pidOrName,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
                ListRowDivider()
                Text(
                    modifier = Modifier.then(itemPadding).weight(1f),
                    text = item.body,
                    fontSize = 14.sp,
                )
            }
            Spacer(
                modifier = Modifier
                    .background(Color.LightGray)
                    .height(1.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ListRowDivider() {
    Divider(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(Color.LightGray)
    )
}
