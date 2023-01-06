@file:Suppress("UnusedReceiverParameter")

package net.matsudamper.android.debugtool.compose.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

object MyIcons {
    public val RocketLauncher: Painter
        @Composable get() {
            return painterResource("drawable/rocket_launch_FILL0_wght400_GRAD0_opsz48.svg")
        }
    public val Storage: Painter
        @Composable get() {
            return painterResource("drawable/storage_FILL0_wght400_GRAD0_opsz48.svg")
        }

    public val Cloud: Painter
        @Composable get() {
            return painterResource("drawable/cloud_FILL0_wght400_GRAD0_opsz48.svg")
        }

    public val VerticalAlignTop: Painter
        @Composable get() {
            return painterResource("drawable/vertical_align_top_black_24dp.svg")
        }
}
