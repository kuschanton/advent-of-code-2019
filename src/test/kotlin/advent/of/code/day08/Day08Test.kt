package advent.of.code.day08

import advent.of.code.TestUtils.Companion.readTextFrom
import arrow.core.right
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class Day08Test {

    @ParameterizedTest(name = "test 1.{index}")
    @MethodSource("testInput1")
    internal fun test1(input: String, height:Int, width: Int, expected: Int) {
        expectThat(findLayer(input, height, width))
            .isEqualTo(expected)
    }

    @ParameterizedTest(name = "test 2.{index}")
    @MethodSource("testInput2")
    internal fun test2(input: String, height:Int, width: Int, expected: String) {
        expectThat(
            buildImage(input, height, width).let { drawImage(it, width) }
        ).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun testInput1() =
            listOf(
                Arguments.of(
                    "121256789012",
                    2,
                    3,
                    4
                ),
                Arguments.of(
                    taskInput(),
                    6,
                    25,
                    2064
                )
            )

        @JvmStatic
        private fun testInput2() =
            listOf(
                Arguments.of(
                    "0222112222120000",
                    2,
                    2,
                    """
                         X
                        X """.trimIndent()
                ),
                // KAUZA
                Arguments.of(
                    taskInput(),
                    6,
                    25,
                    """
                        X  X  XX  X  X XXXX  XX  
                        X X  X  X X  X    X X  X 
                        XX   X  X X  X   X  X  X 
                        X X  XXXX X  X  X   XXXX 
                        X X  X  X X  X X    X  X 
                        X  X X  X  XX  XXXX X  X 
                    """.trimIndent()
                )
            )

        private fun taskInput() = readTextFrom("08_1.txt")

    }
}