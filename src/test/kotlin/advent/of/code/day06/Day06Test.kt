package advent.of.code.day06

import advent.of.code.TestUtils.Companion.readTextFrom
import arrow.core.right
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class Day06Test {

    @ParameterizedTest(name = "test 1.{index} expected value {1}")
    @MethodSource("testInput1")
    internal fun test1(input: String, expected: Int) {
        expectThat(countOrbits(input))
            .isEqualTo(expected.right())
    }

    @ParameterizedTest(name = "test 2.{index} expected value {1}")
    @MethodSource("testInput2")
    internal fun test2(input: String, expected: Int) {
        expectThat(findPath(input))
            .isEqualTo(expected.right())
    }

    companion object {
        @JvmStatic
        private fun testInput1() =
            listOf(
                Arguments.of(
                    """
                        COM)B
                        B)C
                        C)D
                        D)E
                        E)F
                        B)G
                        G)H
                        D)I
                        E)J
                        J)K
                        K)L
                    """.trimIndent(),
                    42
                ),
                Arguments.of(
                    taskInput1(),
                    333679
                )
            )

        @JvmStatic
        private fun testInput2() =
            listOf(
                Arguments.of(
                    """
                        COM)B
                        B)C
                        C)D
                        D)E
                        E)F
                        B)G
                        G)H
                        D)I
                        E)J
                        J)K
                        K)L
                        K)YOU
                        I)SAN
                    """.trimIndent(),
                    4
                ),
                Arguments.of(
                    taskInput2(),
                    370
                )
            )

        private fun taskInput1() = readTextFrom("06_1.txt")
        private fun taskInput2() = readTextFrom("06_2.txt")

    }
}