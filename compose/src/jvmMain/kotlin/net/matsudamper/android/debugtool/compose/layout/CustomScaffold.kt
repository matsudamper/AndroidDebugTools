package net.matsudamper.android.debugtool.compose.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex

@Composable
internal fun CustomScaffold(
    modifier: Modifier,
    drawerContent: @Composable () -> Unit,
    topAppBar: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .zIndex(2f),
        ) {
            drawerContent()
        }
        Column {
            Box(
                Modifier.fillMaxWidth()
                    .zIndex(2f)
            ) {
                topAppBar()
            }
            Box(
                Modifier.fillMaxWidth()
                    .zIndex(1f)
            ) {
                content()
            }
        }
    }
}
