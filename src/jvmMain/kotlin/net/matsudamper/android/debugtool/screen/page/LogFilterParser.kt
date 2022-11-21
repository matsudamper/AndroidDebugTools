package net.matsudamper.android.debugtool.screen.page

class LogFilterParser {
    fun parse(text: String): List<Result> {
        val results = mutableListOf(
            ParseFirstResult(
                remaining = text,
                keyValue = null,
                freeText = null,
                consumeSize = 0,
            )
        )
        do {
            results.add(parseFirst(results.last().remaining))
        } while (results.last().remaining.isNotEmpty())

        var beforeConsumeSize = 0
        return results.drop(1).mapNotNull map@{
            try {
                if (it.keyValue != null) {
                    val (key, value) = it.keyValue
                    return@map Result.KeyValue(
                        key = key,
                        value = value,
//                        originalText = it,
                        start = beforeConsumeSize,
                        end = beforeConsumeSize + it.consumeSize,
                    )
                }

                if (!it.freeText.isNullOrBlank()) {
                    return@map Result.FreeText(
                        text = it.freeText,
//                        originalText = it.freeText,
                        start = beforeConsumeSize,
                        end = beforeConsumeSize + it.consumeSize,
                    )
                }

                return@map null
            } finally {
                beforeConsumeSize += it.consumeSize
            }
        }
    }

    private fun parseFirst(text: String): ParseFirstResult {
        run {
            val trimText = text.trimStart()
            if (text != trimText) {
                return ParseFirstResult(
                    remaining = trimText,
                    freeText = null,
                    keyValue = null,
                    consumeSize = text.length - trimText.length,
                )
            }
        }

        val keyMatchResult = """^(.+?):""".toRegex().find(text)
        if (keyMatchResult != null) {
            val key = keyMatchResult.groups[1]!!.value
            val enumKey = LogFilterKey.values()
                .firstOrNull { it.filterKeyName == key }

            if (enumKey != null) {
                val valueCandidate = text.substring(keyMatchResult.value.length)

                val quotedTextParseResult = parseTextBlock(valueCandidate)

                return ParseFirstResult(
                    remaining = valueCandidate.removePrefix(quotedTextParseResult.original),
                    freeText = null,
                    keyValue = enumKey to quotedTextParseResult.result,
                    consumeSize = quotedTextParseResult.original.length
                        .plus(key.length + 1), // key:
                )
            }
        }

        val result = parseTextBlock(text)
        return ParseFirstResult(
            remaining = text.removePrefix(result.original),
            freeText = result.result,
            keyValue = null,
            consumeSize = result.original.length,
        )
    }

    private fun parseTextBlock(text: String): TextBlockParseResult {
        fun getQuotedTextBlock(text: String): TextBlockParseResult? {
            if (text.startsWith("\"").not()) {
                return null
            }
            val targetText = text.removePrefix("\"")

            val doubleQuote = "\"".toRegex().findAll(targetText).map { it.range.first }

            val escapeQuote = """\\"""".toRegex().findAll(targetText).map {
                it.range.last
            }

            val endQuote = doubleQuote.minus(escapeQuote.toSet()).firstOrNull() ?: return null

            return TextBlockParseResult(
                original = text.substring(0, endQuote + 2),
                result = targetText.substring(0, endQuote).replace("\\\"", "\""),
            )
        }

        val quotedTextBlockResult = getQuotedTextBlock(text)
        if (quotedTextBlockResult != null) {
            return quotedTextBlockResult
        }

        val textMatchResult = """^(\S*?)(?:\s|$)""".toRegex().find(text)
        return if (textMatchResult != null) {
            val result = textMatchResult.groups[1]!!.value
            TextBlockParseResult(
                result = result,
                original = result,
            )
        } else {
            TextBlockParseResult("", "")
        }
    }


    private data class ParseFirstResult(
        val remaining: String,
        val freeText: String?,
        val keyValue: Pair<LogFilterKey?, String>?,
        val consumeSize: Int,
    )

    private data class TextBlockParseResult(
        val original: String,
        val result: String,
    )

    sealed interface Result {
        //        val originalText: String
        val start: Int
        val end: Int

        data class FreeText(
            val text: String,
//            override val originalText: String,
            override val start: Int,
            override val end: Int,
        ) : Result

        data class KeyValue(
            val key: LogFilterKey?,
            val value: String,
//            override val originalText: String,
            override val start: Int,
            override val end: Int,
        ) : Result
    }
}

enum class LogFilterKey(val filterKeyName: String) {
    Package("package"),
    Tag("tag"),
    ;
}
