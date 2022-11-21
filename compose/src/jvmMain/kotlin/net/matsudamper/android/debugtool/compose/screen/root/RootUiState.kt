package net.matsudamper.android.debugtool.compose.screen.root

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

data class RootUiState(
    val minimalDrawer: Boolean,
    val currentPageType: PageType,
    val drawerItems: List<DrawerItem>,
    val devices: List<Device>,
    val currentDevice: String,
    val errorDialog: ErrorDialog?,
    val listener: Listener,
) {
    data class ErrorDialog(
        val title: AnnotatedString,
        val errorText: String,
        val onClickClose: () -> Unit,
        val onClickTitle: (index: Int) -> Unit,
    )

    data class DrawerItem(
        val title: String,
        val type: PageType,
        val color: Color?,
        val listener: Listener,
    ) {
        interface Listener {
            fun onClick()
        }
    }

    data class Device(
        val name: String,
        val onClick: () -> Unit,
    )

    enum class PageType {
        Images,
        Logs,
        Volume,
        ;
    }

    interface Listener {
        fun onClickMenuExpand()
        fun onClickRefresh()
    }
}