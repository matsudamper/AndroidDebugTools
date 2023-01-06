package net.matsudamper.android.debugtool.compose.resources

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

enum class AnnotatedStringTag(val tagName: String) {
    Url("url"),
    ;

    companion object {
        private val tagNamesMap = values().map { it.tagName to it }.toMap()
        fun valueOfOrNull(tagName: String): AnnotatedStringTag? {
            return tagNamesMap[tagName]
        }
    }
}

fun AnnotatedString.applyStyle(colors: ColorScheme): AnnotatedString {
    val original = this
    return buildAnnotatedString {
        append(original)
        getStringAnnotations(0, text.length).forEach { annotatedInfo ->
            when (AnnotatedStringTag.valueOfOrNull(annotatedInfo.tag)) {
                AnnotatedStringTag.Url -> {
                    addStyle(
                        SpanStyle(
                            color = colors.onPrimary,
                        ),
                        start = annotatedInfo.start,
                        end = annotatedInfo.end,
                    )
                }

                null -> Unit
            }
        }
    }
}
