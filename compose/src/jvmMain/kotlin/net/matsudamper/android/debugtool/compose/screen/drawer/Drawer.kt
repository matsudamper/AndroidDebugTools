package net.matsudamper.android.debugtool.compose.screen.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.matsudamper.android.debugtool.compose.resources.MyIcons
import net.matsudamper.android.debugtool.compose.screen.root.RootUiState

@Composable
fun DrawerContent(
    modifier: Modifier,
    minimalDrawer: Boolean,
    onClickMenuExpand: () -> Unit,
    items: List<RootUiState.DrawerItem>,
) {
    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
        ) {
            Header(
                modifier = Modifier.fillMaxWidth(),
                onClickMenuExpand = onClickMenuExpand,
                minimalDrawer = minimalDrawer,
            )
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .then(
                            if (item.color != null) {
                                Modifier.background(item.color)
                            } else {
                                Modifier
                            }
                        )
                        .clickable {
                            item.listener.onClick()
                        }
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (item.type) {
                        RootUiState.PageType.Logs -> {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                            )
                        }

                        RootUiState.PageType.Images -> {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = MyIcons.Cloud,
                                contentDescription = null,
                            )
                        }

                        RootUiState.PageType.Volume -> {
                            Icon(
                                modifier = Modifier.size(32.dp),
                                painter = MyIcons.Storage,
                                contentDescription = null,
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = minimalDrawer.not(),
                    ) {
                        Row {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                fontSize = 24.sp,
                            )
                        }
                    }
                }
                Spacer(
                    Modifier.fillMaxWidth()
                        .height(1.dp)
                        .background(Color.LightGray)
                )
            }
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier,
    onClickMenuExpand: () -> Unit,
    minimalDrawer: Boolean,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
    ) {
        CompositionLocalProvider(
            LocalContentAlpha provides 1.0f,
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                Box(
                    Modifier.clickable(
                        indication = rememberRipple(bounded = false),
                        interactionSource = remember { MutableInteractionSource() },
                    ) { onClickMenuExpand() }
                ) {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        imageVector = Icons.Default.Menu,
                        contentDescription = "menu",
                    )
                }
                Spacer(Modifier.width(8.dp))
                AnimatedVisibility(minimalDrawer.not()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Menu",
                        fontSize = 24.sp,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val listener = object : RootUiState.DrawerItem.Listener {
        override fun onClick() {}
    }
    DrawerContent(
        modifier = Modifier.fillMaxHeight(),
        minimalDrawer = false,
        onClickMenuExpand = {},
        items = listOf(
            RootUiState.DrawerItem(
                title = "container",
                type = RootUiState.PageType.Logs,
                color = null,
                listener = listener,
            ),
            RootUiState.DrawerItem(
                title = "images",
                type = RootUiState.PageType.Images,
                color = null,
                listener = listener,
            ),
            RootUiState.DrawerItem(
                title = "volumes",
                type = RootUiState.PageType.Volume,
                color = null,
                listener = listener,
            )
        )
    )
}
