package advent.of.code.day01

import advent.of.code.day02.executeOperations
import arrow.core.right
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import readInputFrom
import strikt.api.expectThat

internal class Day02Test {

    @ParameterizedTest(name = "test {index} expected value {1}")
    @MethodSource("testInput")
    internal fun test1(input: List<Int>, expected: Int) {
        expectThat(fuelForModules(input))
    }

    fun part1(): Unit {
        listOf(
            listOf(1, 0, 0, 0, 99) to listOf(2, 0, 0, 0, 99),
            listOf(2, 4, 4, 5, 99, 0) to listOf(2, 4, 4, 5, 99, 9801),
            listOf(1, 1, 1, 4, 99, 5, 6, 0, 99) to listOf(30, 1, 1, 4, 2, 5, 6, 0, 99)
        ).forEach {
            println("Input: ${it.first}")
            println("Expectation: ${it.second}")
            println("Result: ${executeOperations(it.first) == it.second.right()}")
        }
        println(executeOperations(advent.of.code.day02.taskInput))
    }

    companion object {
        @JvmStatic
        private fun testInput() =
            listOf(
                Arguments.of(listOf(12), 2),
                Arguments.of(listOf(14), 2),
                Arguments.of(listOf(1969), 654),
                Arguments.of(listOf(100756), 33583),
                Arguments.of(taskInput(), 4973616)
            )

        private fun taskInput() = readInputFrom("01_1.txt")
            .map(String::toInt)
    }
}