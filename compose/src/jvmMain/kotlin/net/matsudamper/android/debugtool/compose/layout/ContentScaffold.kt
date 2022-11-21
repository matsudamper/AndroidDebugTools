package net.matsudamper.android.debugtool.compose.layout

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ContentScaffold(
    modifier: Modifier,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 150.dp),
            elevation = 1.dp,
        ) {
            Box(Modifier.padding(8.dp)) {
                title()
            }
        }
        Box(Modifier) {
            content()
        }
    }
}