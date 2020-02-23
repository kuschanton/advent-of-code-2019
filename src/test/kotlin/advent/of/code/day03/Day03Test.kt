package advent.of.code.day03

import advent.of.code.TestUtils.Companion.readLinesFrom
import arrow.core.right
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class Day03Test {

    @ParameterizedTest(name = "test 1.{index} expected value {1}")
    @MethodSource("testInput1")
    internal fun test1(input: Pair<String, String>, expected: Int) {
        expectThat(findClosestIntersection(input.first, input.second))
            .isEqualTo(expected.right())
    }

    @ParameterizedTest(name = "test 2.{index} expected value {1}")
    @MethodSource("testInput2")
    internal fun test2(input: Pair<String, String>, expected: Int) {
        expectThat(findShortestCircuit(input.first, input.second))
            .isEqualTo(expected.right())
    }

    companion object {
        @JvmStatic
        private fun testInput1() =
            listOf(
                Arguments.of(
                    "R8,U5,L5,D3" to "U7,R6,D4,L4",
                    6
                ),
                Arguments.of(
                    "R75,D30,R83,U83,L12,D49,R71,U7,L72" to "U62,R66,U55,R34,D71,R55,D58,R83",
                    159
                ),
                Arguments.of(
                    "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51" to "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
                    135
                ),
                Arguments.of(
                    taskInput(),
                    2129
                )
            )

        @JvmStatic
        private fun testInput2() =
            listOf(
                Arguments.of(
                    "R8,U5,L5,D3" to "U7,R6,D4,L4",
                    30
                ),
                Arguments.of(
                    "R75,D30,R83,U83,L12,D49,R71,U7,L72" to "U62,R66,U55,R34,D71,R55,D58,R83",
                    610
                ),
                Arguments.of(
                    "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51" to "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
                    410
                ),
                Arguments.of(
                    taskInput(),
                    134662
                )
            )

        private fun taskInput() = readLinesFrom("03_1.txt")
            .let { it[0] to it[1] }

    }
}