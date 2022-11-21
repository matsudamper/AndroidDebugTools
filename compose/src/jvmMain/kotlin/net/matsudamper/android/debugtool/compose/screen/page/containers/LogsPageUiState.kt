package net.matsudamper.android.debugtool.compose.screen.page.containers

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import java.io.Serializable

data class LogsPageUiState(
    val logs: List<Log>,
    val order: List<Order>,
    val filterSuggests: List<FilterSuggest>,
    val filterText: TextFieldValue,
    val listener: Listener,
) {
    data class FilterSuggest(
        val text: String,
//        val onClick: () -> Unit,
    )
    data class Log(
        val key: Serializable,
        val time: String,
        val pidOrName: String,
        val body: String,
    )
    data class Container(
        val id: String,
        val names: String,
        val image: String,
        val date: String,
    )

    enum class Order {
        Id,
        Names,
        Image,
        Date,
        ;
    }

    @Stable
    interface Listener {
        fun onResume()
        fun onFilterTextChanged(textFieldValue: TextFieldValue)
        fun onPressSuggestEscape()
    }
}