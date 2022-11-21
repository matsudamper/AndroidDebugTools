package net.matsudamper.android.debugtool.compose.resources

import androidx.compose.foundation.LocalIndication
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalIndication provides rememberRipple(color = Color.Red)
    ) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                primary = Color(0xFF00b0ff),
                surfaceVariant = Color.AliceBlue, // アクションアイテムの背景
                outline = MaterialTheme.colorScheme.outline, // アクションアイテムのOutline
            )
        ) {
            content()
        }
    }
}