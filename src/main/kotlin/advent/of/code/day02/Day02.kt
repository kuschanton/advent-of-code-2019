package advent.of.code.day02

import advent.of.code.Error
import advent.of.code.replaceAtIndex
import arrow.core.*
import arrow.core.extensions.fx
import java.lang.Integer.max


fun main() {
    permutations()
        .map {
            executeOperations(
                taskInput.replaceAtIndex(
                    1,
                    it.first
                ).replaceAtIndex(2, it.second)
            )
                .map { result -> Triple(it.first, it.second, result[0]) }
        }.singleOrNull {
            when (it) {
                is Either.Right -> it.b.third == 19690720
                else -> false
            }
        }?.map {
            println(it)
            println(100 * it.first + it.second)
        }
}

fun permutations() = (0..100).flatMap { noun ->
    (0..100).map { verb -> noun to verb }
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
    println(executeOperations(taskInput))
}

fun executeOperations(input: List<Int>): Either<Error, List<Int>> {
    fun go(index: Int, input: List<Int>): Either<Error, List<Int>> = Either.fx {
        val (commandTuple) = input.nextFourFrom(index)
        val (command) = commandTuple.toOperation()
        when (command) {
            is Halt -> input.right()
            is ExecutableOperation -> go(index + 4, command.executeOn(input).bind())
        }.bind()
    }

    return go(0, input)
}

fun ExecutableOperation.executeOn(input: List<Int>): Either<Error, List<Int>> =
    if (max(pos1, pos2, targetPos) < input.size)
        input.replaceAtIndex(targetPos, f(input[pos1], input[pos2])).right()
    else
        Error("Not able to execute $this on input: out of range").left()

fun max(a: Int, b: Int, c: Int) = max(max(a, b), c)

fun CommandTuple<Int>.toOperation() = when {
    a == 99 -> Halt.right()
    a == 1 && b != null && c != null && d != null -> Add(b, c, d).right()
    a == 2 && b != null && c != null && d != null -> Multiply(b, c, d).right()
    else -> Error("Not able to construct command from $this").left()
}

fun <T> List<T>.nextFourFrom(index: Int): Either<Error, CommandTuple<T>> =
    if (index < size)
        CommandTuple(
            this[index],
            getOrNull(index + 1),
            getOrNull(index + 2),
            getOrNull(index + 3)
        ).right()
    else Error("Next four is out of range. Index: $index and list $this").left()

sealed class Operation

object Halt : Operation()

sealed class ExecutableOperation(val f: (Int, Int) -> Int) : Operation() {
    abstract val pos1: Int
    abstract val pos2: Int
    abstract val targetPos: Int
}

data class Add(
    override val pos1: Int,
    override val pos2: Int,
    override val targetPos: Int
) : ExecutableOperation(Int::plus)

data class Multiply(
    override val pos1: Int,
    override val pos2: Int,
    override val targetPos: Int
) : ExecutableOperation(Int::times)

data class CommandTuple<T>(val a: T, val b: T?, val c: T?, val d: T?)

val taskInput = listOf(
    1,
    12,
    2,
    3,
    1,
    1,
    2,
    3,
    1,
    3,
    4,
    3,
    1,
    5,
    0,
    3,
    2,
    10,
    1,
    19,
    1,
    19,
    9,
    23,
    1,
    23,
    6,
    27,
    1,
    9,
    27,
    31,
    1,
    31,
    10,
    35,
    2,
    13,
    35,
    39,
    1,
    39,
    10,
    43,
    1,
    43,
    9,
    47,
    1,
    47,
    13,
    51,
    1,
    51,
    13,
    55,
    2,
    55,
    6,
    59,
    1,
    59,
    5,
    63,
    2,
    10,
    63,
    67,
    1,
    67,
    9,
    71,
    1,
    71,
    13,
    75,
    1,
    6,
    75,
    79,
    1,
    10,
    79,
    83,
    2,
    9,
    83,
    87,
    1,
    87,
    5,
    91,
    2,
    91,
    9,
    95,
    1,
    6,
    95,
    99,
    1,
    99,
    5,
    103,
    2,
    103,
    10,
    107,
    1,
    107,
    6,
    111,
    2,
    9,
    111,
    115,
    2,
    9,
    115,
    119,
    2,
    13,
    119,
    123,
    1,
    123,
    9,
    127,
    1,
    5,
    127,
    131,
    1,
    131,
    2,
    135,
    1,
    135,
    6,
    0,
    99,
    2,
    0,
    14,
    0
)