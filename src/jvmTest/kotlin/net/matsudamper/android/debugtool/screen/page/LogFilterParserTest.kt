package net.matsudamper.android.debugtool.screen.page

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class LogFilterParserTest : DescribeSpec({
    describe("パースが行える") {
        context("スペースがある場合、一文字ずつ識別される") {
            val text = "package:net.matsudamper L O G"
            it("パースできる") {
                LogFilterParser().parse(text)
                    .shouldBe(
                        listOf(
                            LogFilterParser.Result.KeyValue(
                                LogFilterKey.Package,
                                value = "net.matsudamper",
                                start = 0,
                                end = 23,
                            ),
                            LogFilterParser.Result.FreeText(
                                "L",
                                start = 24,
                                end = 25,
                            ),

                            LogFilterParser.Result.FreeText(
                                "O",
                                start = 26,
                                end = 27,
                            ),

                            LogFilterParser.Result.FreeText(
                                "G",
                                start = 28,
                                end = 29,
                            ),
                        )
                    )
            }
        }
        context("ダブルクオーテーションで囲まれている場合") {
            val text = """package:net.matsudamper "L O G""""
            it("ダブルクオーテーション内のテキストがまとまって認識される") {
                LogFilterParser().parse(text)
                    .shouldBe(
                        listOf(
                            LogFilterParser.Result.KeyValue(
                                LogFilterKey.Package,
                                value = "net.matsudamper",
                                start = 0,
                                end = 23,
                            ),
                            LogFilterParser.Result.FreeText(
                                text = "L O G",
                                start = 24,
                                end = 31,
                            )
                        )
                    )
            }
        }
        context("valueがダブルクオーテーションで囲まれており、バッククオートでダブルクオーテーションがエスケープされている") {
            val text = """package:net.matsudamper tag:" \"L O G\" """"
            it("バッククオートでダブルクオーテーションがエスケープされる") {
                LogFilterParser().parse(text)
                    .shouldBe(
                        listOf(
                            LogFilterParser.Result.KeyValue(
                                LogFilterKey.Package,
                                value = "net.matsudamper",
                                start = 0,
                                end = 23,
                            ),
                            LogFilterParser.Result.KeyValue(
                                LogFilterKey.Tag,
                                value = " \"L O G\" ",
                                start = 24,
                                end = 41,
                            ),
                        )
                    )
            }
        }
        context("keyがダブルクオーテーションで囲まれており、エスケープされている") {
            val text = """ "one \"two\" three" """
            it("バッククオートでダブルクオーテーションがエスケープされる") {
                LogFilterParser().parse(text)
                    .shouldBe(
                        listOf(
                            LogFilterParser.Result.FreeText(
                                text = "one \"two\" three",
                                start = 1,
                                end = 20,
                            ),
                        )
                    )
            }
        }
        context("コロンが入っていても1つのテキストとして認識される") {
            val text = """https://google.com"""
            it("バッククオートでダブルクオーテーションがエスケープされる") {
                LogFilterParser().parse(text)
                    .shouldBe(
                        listOf(
                            LogFilterParser.Result.FreeText(
                                text = text,
                                start = 0,
                                end = text.length,
                            ),
                        )
                    )
            }
        }
    }
})
