package advent.of.code.day01

import advent.of.code.TestUtils.Companion.readLinesFrom
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class Day01Test {

    @ParameterizedTest(name = "test 1.{index} expected value {1}")
    @MethodSource("testInputPart1")
    internal fun part1Test(input: List<Int>, expected: Int) {
        expectThat(fuelForModules(input)).isEqualTo(expected)
    }

    @ParameterizedTest(name = "test 2.{index} expected value {1}")
    @MethodSource("testInputPart2")
    internal fun part2Test(input: List<Int>, expected: Int) {
        expectThat(fuelForModulesAndFuel(input)).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun testInputPart1() =
            listOf(
                Arguments.of(listOf(12), 2),
                Arguments.of(listOf(14), 2),
                Arguments.of(listOf(1969), 654),
                Arguments.of(listOf(100756), 33583),
                Arguments.of(taskInput(), 3317659)
            )

        @JvmStatic
        private fun testInputPart2() =
            listOf(
                Arguments.of(listOf(12), 2),
                Arguments.of(listOf(14), 2),
                Arguments.of(listOf(1969), 966),
                Arguments.of(listOf(100756), 50346),
                Arguments.of(taskInput(), 4973616)
            )

        private fun taskInput() = readLinesFrom("01_1.txt")
            .map(String::toInt)
    }
}