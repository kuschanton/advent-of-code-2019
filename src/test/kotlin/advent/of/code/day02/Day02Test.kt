package advent.of.code.day02

import advent.of.code.TestUtils.Companion.readTextFrom
import advent.of.code.replaceAtIndex
import arrow.core.right
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.arrow.b
import strikt.arrow.isRight
import strikt.assertions.get
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

internal class Day02Test {

    @ParameterizedTest(name = "test {index} expected value {1}")
    @MethodSource("testInput")
    internal fun test1(input: List<Int>, expected: List<Int>) {
        expectThat(executeOperations(input))
            .isEqualTo(expected.right())
    }

    @Test
    internal fun task1() {
        val modifiedInput = taskInput()
            .replaceAtIndex(1, 12)
            .replaceAtIndex(2, 2)

        expectThat(executeOperations(modifiedInput))
            .isRight()
            .and { b.isNotEmpty() }
            .and { b[0].isEqualTo(4138658) }
    }

    @Test
    internal fun task2() {
        expectThat(findMatchingPair(taskInput(), 19690720))
            .isEqualTo(7264)
    }

    companion object {
        @JvmStatic
        private fun testInput() =
            listOf(
                Arguments.of(
                    listOf(1, 0, 0, 0, 99),
                    listOf(2, 0, 0, 0, 99)
                ),
                Arguments.of(
                    listOf(2, 4, 4, 5, 99, 0),
                    listOf(2, 4, 4, 5, 99, 9801)
                ),
                Arguments.of(
                    listOf(1, 1, 1, 4, 99, 5, 6, 0, 99),
                    listOf(30, 1, 1, 4, 2, 5, 6, 0, 99)
                )
            )

        private fun taskInput() = readTextFrom("02_1`.txt")
            .split(',')
            .map(String::toInt)
    }
}