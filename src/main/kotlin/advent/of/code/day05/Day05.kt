package advent.of.code.day05

import advent.of.code.Error
import advent.of.code.digits
import advent.of.code.replaceAtIndex
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import java.lang.Integer.max

fun executeOperations(
    input: List<Int>,
    receiveChannel: ReceiveChannel<Int>,
    sendChannel: SendChannel<Int>
    ): Either<Error, List<Int>> {
    fun go(index: Int, input: List<Int>): Either<Error, List<Int>> = Either.fx {
        val (commandTuple) = input.nextFourFrom(index)
        val (command) = commandTuple.toOperation(index)
        when (command) {
            is Halt -> input.right()
            is ExecutableOperation -> go(index + command.size, command.executeOn(input).bind())
            is ReadInput -> go(index + command.size, command.executeOn(input, receiveChannel))
            is WriteOutput -> go(index + command.size, command.executeOn(input, sendChannel).bind())
            is JumpIfTrue -> go(command.evaluateNewIndex(index, input), input)
            is JumpIfFalse -> go(command.evaluateNewIndex(index, input), input)
            is LessThan -> go(index + command.size, command.executeOn(input).bind())
            is Equals -> go(index + command.size, command.executeOn(input).bind())
        }.bind()
    }

    return go(0, input)
}

fun ComparisonOperation.executeOn(input: List<Int>): Either<Error, List<Int>> =
    if (max(pos1, pos2, targetPos) < input.size) {
        val result = if (f(input[pos1], input[pos2])) 1 else 0
        input.replaceAtIndex(targetPos, result).right()
    } else
        Error("Not able to execute $this on input: out of range").left()

fun JumpOperation.evaluateNewIndex(currentIndex: Int, input: List<Int>): Int =
    if (f(input[pos1])) input[targetPos]
    else currentIndex + size

fun WriteOutput.executeOn(input: List<Int>, sendChannel: SendChannel<Int>): Either<Error, List<Int>> =
    if (targetPos < input.size) {
        sendChannel.offer(input[targetPos])
        input.right()
    } else Error("Not able to execute $this on input: out of range").left()

fun ReadInput.executeOn(input: List<Int>, receiveChannel: ReceiveChannel<Int>): List<Int> =
    runBlocking { receiveChannel.receive() }
        .let { input.replaceAtIndex(targetPos, it) }

fun ExecutableOperation.executeOn(input: List<Int>): Either<Error, List<Int>> =
    if (max(pos1, pos2, targetPos) < input.size)
        input.replaceAtIndex(targetPos, f(input[pos1], input[pos2])).right()
    else
        Error("Not able to execute $this on input: out of range").left()

fun max(a: Int, b: Int, c: Int) = max(max(a, b), c)

fun CommandTuple<Int>.toOperation(index: Int): Either<Error, Operation> {
    val commandDigits = a.toDigitsWithLeadingZeros()
    val (mode2, mode1, opcode2, opcode1) = commandDigits
    return when {
        opcode1 == 1 && b != null && c != null && d != null ->
            constructTwoArgOperation(mode1, b, mode2, c, d, index, ::Add)
        opcode1 == 2 && b != null && c != null && d != null ->
            constructTwoArgOperation(mode1, b, mode2, c, d, index, ::Multiply)
        opcode1 == 3 && b != null -> ReadInput(b).right()
        opcode1 == 4 && b != null -> WriteOutput(if (mode1 == 0) b else index + 1).right()
        opcode1 == 5 && b != null && c != null -> constructJumpOperation(mode1, b, mode2, c, index, ::JumpIfTrue)
        opcode1 == 6 && b != null && c != null -> constructJumpOperation(mode1, b, mode2, c, index, ::JumpIfFalse)
        opcode1 == 7 && b != null && c != null && d != null ->
            constructTwoArgOperation(mode1, b, mode2, c, d, index, ::LessThan)
        opcode1 == 8 && b != null && c != null && d != null ->
            constructTwoArgOperation(mode1, b, mode2, c, d, index, ::Equals)
        opcode1 == 9 && opcode2 == 9 -> Halt.right()
        else -> Error("Not able to construct command from $this").left()
    }
}

fun constructJumpOperation(
    mode1: Int,
    arg1: Int,
    mode2: Int,
    arg2: Int,
    index: Int,
    constructor: (Int, Int) -> JumpOperation
): Either<Error, JumpOperation> = Either.fx {
    val (arg1position) = evaluateArgumentPosition(index + 1, mode1, arg1)
    val (targetCursorPosition) = evaluateArgumentPosition(index + 2, mode2, arg2)
    constructor(arg1position, targetCursorPosition)
}

// We still gonna use positions for arguments if it's immediate mode, just use index to evaluate position
fun constructTwoArgOperation(
    mode1: Int,
    arg1: Int,
    mode2: Int,
    arg2: Int,
    targetPos: Int,
    index: Int,
    constructor: (Int, Int, Int) -> Operation
): Either<Error, Operation> = Either.fx {
    val (arg1position) = evaluateArgumentPosition(index + 1, mode1, arg1)
    val (arg2position) = evaluateArgumentPosition(index + 2, mode2, arg2)
    constructor(arg1position, arg2position, targetPos)
}

fun evaluateArgumentPosition(index: Int, mode: Int, value: Int): Either<Error, Int> =
    when (mode) {
        0 -> value.right()
        1 -> index.right()
        else -> Error("This mode is not supported: $mode").left()
    }

fun Int.toDigitsWithLeadingZeros(): List<Int> {
    val digits = this.digits()
    return if (digits.size < 4) List(4 - digits.size) { 0 } + digits
    else digits
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

sealed class Operation {
    abstract val size: Int
}

object Halt : Operation() {
    override val size: Int = 1
}

sealed class ExecutableOperation(val f: (Int, Int) -> Int) : Operation() {
    abstract val pos1: Int
    abstract val pos2: Int
    abstract val targetPos: Int
    override val size: Int = 4
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

data class ReadInput(
    val targetPos: Int
) : Operation() {
    override val size: Int = 2
}

data class WriteOutput(
    val targetPos: Int
) : Operation() {
    override val size: Int = 2
}

sealed class JumpOperation(val f: (Int) -> Boolean) : Operation() {
    abstract val pos1: Int
    abstract val targetPos: Int
    override val size: Int = 3
}

data class JumpIfTrue(
    override val pos1: Int,
    override val targetPos: Int
) : JumpOperation({ it != 0 })

data class JumpIfFalse(
    override val pos1: Int,
    override val targetPos: Int
) : JumpOperation({ it == 0 })

sealed class ComparisonOperation(val f: (Int, Int) -> Boolean) : Operation() {
    abstract val pos1: Int
    abstract val pos2: Int
    abstract val targetPos: Int
    override val size: Int = 4
}

data class LessThan(
    override val pos1: Int,
    override val pos2: Int,
    override val targetPos: Int
) : ComparisonOperation({ a, b -> a < b })

data class Equals(
    override val pos1: Int,
    override val pos2: Int,
    override val targetPos: Int
) : ComparisonOperation({ a, b -> a == b })

data class CommandTuple<T>(val a: T, val b: T?, val c: T?, val d: T?)