package advent.of.code.day10

import advent.of.code.TestUtils.Companion.readTextFrom
import advent.of.code.readInputFromCode
import arrow.core.right
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class Day10Test {

    @ParameterizedTest(name = "test 1.{index}")
    @MethodSource("testInput1")
    internal fun test1(input: String, expected: Triple<Int, Int, Int>) {
        expectThat(findBestPlace(input))
            .isEqualTo(expected)
    }

    @ParameterizedTest(name = "test 2.{index}")
    @MethodSource("testInput2")
    internal fun test2(input: String, index: Int, startingPoint: Pair<Int, Int>, expected: Pair<Int, Int>) {
        expectThat(
            findNVaporized(input, index, startingPoint)
        ).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        private fun testInput1() =
            listOf(
                Arguments.of(
                    """
                        .#....#####...#..
                        ##...##.#####..##
                        ##...#...#.#####.
                        ..#.....X...###..
                        ..#.#.....#....##
                    """.trimIndent(),
                    Triple(30, 8, 3)
                ),
                Arguments.of(
                    """
                        .#..#..###
                        ####.###.#
                        ....###.#.
                        ..###.##.#
                        ##.##.#.#.
                        ....###..#
                        ..#.#..#.#
                        #..#.#.###
                        .##...##.#
                        .....#.#..
                    """.trimIndent(),
                    Triple(41, 6, 3)
                ),
                Arguments.of(
                    """
                        .#..##.###...#######
                        ##.############..##.
                        .#.######.########.#
                        .###.#######.####.#.
                        #####.##.#.##.###.##
                        ..#####..#.#########
                        ####################
                        #.####....###.#.#.##
                        ##.#################
                        #####.##.###..####..
                        ..######..##.#######
                        ####.##.####...##..#
                        .#####..#.######.###
                        ##...#.##########...
                        #.##########.#######
                        .####.#.###.###.#.##
                        ....##.##.###..#####
                        .#.#.###########.###
                        #.#.#.#####.####.###
                        ###.##.####.##.#..##
                    """.trimIndent(),
                    Triple(210, 11, 13)
                ),
                Arguments.of(
                    taskInput(),
                    Triple(276, 17, 22)
                )
            )

        @JvmStatic
        private fun testInput2() =
            listOf(
                Arguments.of(
                    """
                        .#..##.###...#######
                        ##.############..##.
                        .#.######.########.#
                        .###.#######.####.#.
                        #####.##.#.##.###.##
                        ..#####..#.#########
                        ####################
                        #.####....###.#.#.##
                        ##.#################
                        #####.##.###..####..
                        ..######..##.#######
                        ####.##.####...##..#
                        .#####..#.######.###
                        ##...#.##########...
                        #.##########.#######
                        .####.#.###.###.#.##
                        ....##.##.###..#####
                        .#.#.###########.###
                        #.#.#.#####.####.###
                        ###.##.####.##.#..##
                    """.trimIndent(),
                    200,
                    Pair(11, 13),
                    Pair(8, 2)
                ),
                Arguments.of(
                    taskInput(),
                    200,
                    Pair(17, 22),
                    Pair(13, 21)
                )
            )

        private fun taskInput() = readTextFrom("10_1.txt")

    }
}