package advent.of.code.day09

import advent.of.code.TestUtils.Companion.readTextFrom
import arrow.core.right
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.arrow.isRight
import strikt.assertions.isEqualTo

internal class Day09Test {

    @ParameterizedTest(name = "test {index}")
    @MethodSource("testInput")
    internal fun test1(taskInput: List<Long>, expected: Long) {
        val additionalMemory = List(taskInput.size * 10) { 0L }
        val channels = List(2) { Channel<Long>(10) }
        val (input, output) = channels

        val ampA = Amp(taskInput + additionalMemory, 2, input, output, null)

        expectThat(ampA.executeOperationsTailRec()).isRight()
        expectThat(runBlocking { output.receive() }).isEqualTo(expected)

        channels.forEach { it.close() }
    }

    companion object {
        @JvmStatic
        private fun testInput() =
            listOf(
                Arguments.of(
                    listOf(104, 1125899906842624, 99),
                    1125899906842624
                ),
                Arguments.of(
                    listOf(1102, 34915192, 34915192, 7, 4, 7, 99, 0),
                    1219070632396864
                ),
                Arguments.of(
                    taskInput(),
                    58534L
                )
            )

        private fun taskInput() = readTextFrom("09_1.txt")
            .split(',')
            .map { it.toLong() }
    }
}