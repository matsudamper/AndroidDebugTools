package net.matsudamper.android.debugtool.compose.screen.page.images

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.matsudamper.android.debugtool.compose.layout.ContentScaffold

@Composable
fun ImagesPage(
    modifier: Modifier
) {
    ContentScaffold(
        modifier = modifier,
        title = {
            Text("Images")
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("aaaaaaaaaaaaaa")
        }
    }
}