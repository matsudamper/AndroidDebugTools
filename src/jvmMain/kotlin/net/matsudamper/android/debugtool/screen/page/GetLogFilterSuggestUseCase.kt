package net.matsudamper.android.debugtool.screen.page

class GetLogFilterSuggestUseCase() {
    fun exec(logFilters: List<LogFilterParser.Result>, cursorIndex: Int): List<SuggestItem> {
        val currentTarget = logFilters.firstOrNull { cursorIndex in it.start..it.end } ?: return emptyList()
        return when (currentTarget) {
            is LogFilterParser.Result.FreeText -> {
                val targets = LogFilterKey.values().filter {
                    if (it.filterKeyName == currentTarget.text && currentTarget.text.isEmpty()) {// TODO 未入力でサジェストが出る
                        return@filter false
                    }
                    it.filterKeyName.contains(currentTarget.text)
                }
                targets.map {
                    SuggestItem(
                        suggestText = it.filterKeyName,
                        newText = "${it.filterKeyName}:",
                        replaceRange = currentTarget.start..currentTarget.end,
                    )
                }
            }

            is LogFilterParser.Result.KeyValue -> {
                return emptyList()
            }
        }
    }

    data class SuggestItem(
        val suggestText: String,
        val newText: String,
        val replaceRange: IntRange,
    )
}
