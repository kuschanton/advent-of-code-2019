package advent.of.code.day05

import advent.of.code.TestUtils.Companion.readTextFrom
import advent.of.code.replaceAtIndex
import arrow.core.right
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.last
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expect
import strikt.api.expectThat
import strikt.arrow.b
import strikt.arrow.isRight
import strikt.assertions.get
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

internal class Day05Test {

    @ParameterizedTest(name = "test {index}")
    @MethodSource("testInput")
    internal fun test1(input: List<Int>, initCode: Int?, expected: List<Int>?, diagnosticCode: Int?) {
        val (receiveChannel, sendChannel) = List(2) { Channel<Int>(10) }

        initCode?.also {
            receiveChannel.offer(it)
        }

        val result = executeOperations(input, receiveChannel, sendChannel)

        expected?.also { expectThat(result).isEqualTo(it.right()) }


        diagnosticCode?.also {
            expectThat(runBlocking { sendChannel.last() }).isEqualTo(it)
        }
    }

//    @Test
//    internal fun task1() {
//        val modifiedInput = taskInput()
//            .replaceAtIndex(1, 12)
//            .replaceAtIndex(2, 2)
//
//        expectThat(executeOperations(modifiedInput))
//            .isRight()
//            .and { b.isNotEmpty() }
//            .and { b[0].isEqualTo(4138658) }
//    }

//    @Test
//    internal fun task2() {
//        expectThat(findMatchingPair(taskInput(), 19690720))
//            .isEqualTo(7264)
//    }


//    fun part1(): Unit {
//        listOf(
//            listOf(3, 21, 1008, 21, 8, 20, 1005, 20, 22, 107, 8, 21, 20, 1006, 20, 31, 1106, 0, 36, 98, 0, 0, 1002, 21, 125, 20, 4, 20, 1105, 1, 46, 104, 999, 1105, 1, 46, 1101, 1000, 1, 20, 4, 20, 1105, 1, 46, 98, 99)
//                    to listOf(1002, 4, 3, 4, 99)
////        listOf(3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8) to listOf(3, 9, 8, 9, 10, 9, 4, 9, 99, 1, 8),
////        listOf(3, 9, 7, 9, 10, 9, 4, 9, 99, -1, 8) to listOf(30, 1, 1, 4, 2, 5, 6, 0, 99),
////        listOf(3, 3, 1107, -1, 8, 3, 4, 3, 99) to listOf(30, 1, 1, 4, 2, 5, 6, 0, 99),
////        listOf(3, 3, 1105, -1, 9, 1101, 0, 0, 12, 4, 12, 99, 1) to listOf(30, 1, 1, 4, 2, 5, 6, 0, 99)
//        ).forEach {
//            println("Input: ${it.first}")
//            println("Expectation: ${it.second}")
//            println("Result: ${executeOperations(it.first)}")
//        }
//        println(executeOperations(taskInput))
//    }

    companion object {
        @JvmStatic
        private fun testInput() =
            listOf(
                Arguments.of(
                    listOf(1002, 4, 3, 4, 33),
                    null,
                    listOf(1002, 4, 3, 4, 99),
                    null
                ),
                Arguments.of(
                    listOf(3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8),
                    8,
                    null,
                    1
                ),
                Arguments.of(
                    listOf(3, 9, 8, 9, 10, 9, 4, 9, 99, -1, 8),
                    7,
                    null,
                    0
                ),
                Arguments.of(
                    listOf(3, 9, 7, 9, 10, 9, 4, 9, 99, -1, 8),
                    7,
                    null,
                    1
                ),
                Arguments.of(
                    listOf(3, 9, 7, 9, 10, 9, 4, 9, 99, -1, 8),
                    9,
                    null,
                    0
                ),
                Arguments.of(
                    listOf(3, 3, 1108, -1, 8, 3, 4, 3, 99),
                    8,
                    null,
                    1
                ),
                Arguments.of(
                    listOf(3, 3, 1108, -1, 8, 3, 4, 3, 99),
                    2,
                    null,
                    0
                ),
                Arguments.of(
                    listOf(3, 3, 1107, -1, 8, 3, 4, 3, 99),
                    7,
                    null,
                    1
                ),
                Arguments.of(
                    listOf(3, 3, 1107, -1, 8, 3, 4, 3, 99),
                    8,
                    null,
                    0
                ),
                Arguments.of(
                    listOf(3, 12, 6, 12, 15, 1, 13, 14, 13, 4, 13, 99, -1, 0, 1, 9),
                    0,
                    null,
                    0
                ),
                Arguments.of(
                    listOf(3, 12, 6, 12, 15, 1, 13, 14, 13, 4, 13, 99, -1, 0, 1, 9),
                    99,
                    null,
                    1
                ),
                Arguments.of(
                    listOf(3, 3, 1105, -1, 9, 1101, 0, 0, 12, 4, 12, 99, 1),
                    0,
                    null,
                    0
                ),
                Arguments.of(
                    listOf(3, 3, 1105, -1, 9, 1101, 0, 0, 12, 4, 12, 99, 1),
                    99,
                    null,
                    1
                ),
                Arguments.of(
                    listOf(
                        3, 21, 1008, 21, 8, 20, 1005, 20, 22, 107, 8, 21, 20, 1006, 20, 31,
                        1106, 0, 36, 98, 0, 0, 1002, 21, 125, 20, 4, 20, 1105, 1, 46, 104,
                        999, 1105, 1, 46, 1101, 1000, 1, 20, 4, 20, 1105, 1, 46, 98, 99
                    ),
                    7,
                    null,
                    999
                ),
                Arguments.of(
                    listOf(
                        3, 21, 1008, 21, 8, 20, 1005, 20, 22, 107, 8, 21, 20, 1006, 20, 31,
                        1106, 0, 36, 98, 0, 0, 1002, 21, 125, 20, 4, 20, 1105, 1, 46, 104,
                        999, 1105, 1, 46, 1101, 1000, 1, 20, 4, 20, 1105, 1, 46, 98, 99
                    ),
                    8,
                    null,
                    1000
                ),
                Arguments.of(
                    listOf(
                        3, 21, 1008, 21, 8, 20, 1005, 20, 22, 107, 8, 21, 20, 1006, 20, 31,
                        1106, 0, 36, 98, 0, 0, 1002, 21, 125, 20, 4, 20, 1105, 1, 46, 104,
                        999, 1105, 1, 46, 1101, 1000, 1, 20, 4, 20, 1105, 1, 46, 98, 99
                    ),
                    9,
                    null,
                    1001
                ),
                Arguments.of(
                    taskInput(),
                    1,
                    null,
                    7566643
                ),
                Arguments.of(
                    taskInput(),
                    5,
                    null,
                    9265694
                )
            )

        private fun taskInput() = readTextFrom("05_1.txt")
            .split(',')
            .map(String::toInt)
    }
}