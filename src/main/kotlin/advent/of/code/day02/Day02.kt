package advent.of.code.day02

import advent.of.code.Error
import advent.of.code.filterRight
import advent.of.code.replaceAtIndex
import arrow.core.*
import arrow.core.extensions.fx
import java.lang.Integer.max


fun findMatchingPair(input: List<Int>, expectation: Int) =
    permutations()
        .map {
            val modifiedInput = input
                .replaceAtIndex(1, it.first)
                .replaceAtIndex(2, it.second)

            executeOperations(modifiedInput)
                .map { result -> Triple(it.first, it.second, result[0]) }
        }
        .filterRight { it.third == expectation }
        .map { 100 * it.first + it.second }
        .singleOrNull()

fun permutations() = (0..100).flatMap { noun ->
    (0..100).map { verb -> noun to verb }
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