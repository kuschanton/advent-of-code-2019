package advent.of.code.day09

import advent.of.code.Error
import advent.of.code.toEitherList
import advent.of.code.digits
import advent.of.code.eitherCatch
import advent.of.code.readInputFromCode
import advent.of.code.replaceAtIndex
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import java.lang.Integer.max


fun main() {
    val result = Either.fx<Error, List<Long>> {
        val program = readInputFromCode("09_1.txt")
            .split(',')
            .map { eitherCatch { it.toLong() } }
            .toEitherList()
            .mapLeft { ex -> Error("Not able to convert to Long: $ex") }
            .bind()

        val additionalMemory = List(program.size * 10) { 0L }

        val channels = List(2) { Channel<Long>(10) }
        val (input, output) = channels

        val ampA = Amp(program + additionalMemory, 2, input, output, null)

        GlobalScope.launch {
            select<Unit> {
                output.onReceive {
                    println(it)
                }
            }
        }

        val result = ampA.executeOperationsTailRec().bind()

        channels.forEach { it.close() }

        result
    }
    println(result.isRight())
}

data class Amp(
    val program: List<Long>,
    val initCode: Long?,
    val receiveChannel: Channel<Long>,
    val sendChannel: SendChannel<Long>,
    val inputValue: Long? = null
) {
    init {
        initCode?.let { receiveChannel.offer(it) }
        inputValue?.let { receiveChannel.offer(it) }
    }
}

fun Amp.executeOperationsTailRec(): Either<Error, List<Long>> = Either.tailRecM(Triple(0, 0, program)) {
    val (index: Int, base: Int, input: List<Long>) = it
    Either.fx<Error, Either<Triple<Int, Int, List<Long>>, List<Long>>> {
        val (commandTuple) = input.nextFourFrom(index)
        val (command) = commandTuple.toOperation(index, base)
        when (command) {
            is Halt -> input.right()
            is ExecutableOperation -> Triple(index + command.size, base, command.executeOn(input).bind()).left()
            is ReadInput -> Triple(index + command.size, base, command.executeOn(input, receiveChannel)).left()
            is WriteOutput -> Triple(index + command.size, base, command.executeOn(input, sendChannel).bind()).left()
            is JumpIfTrue -> Triple(command.evaluateNewIndex(index, input), base, input).left()
            is JumpIfFalse -> Triple(command.evaluateNewIndex(index, input), base, input).left()
            is LessThan -> Triple(index + command.size, base, command.executeOn(input).bind()).left()
            is Equals -> Triple(index + command.size, base, command.executeOn(input).bind()).left()
            is UpdateBase -> Triple(index + command.size, base + input[command.pos1].toInt(), input).left()
        }
    }
}

fun ComparisonOperation.executeOn(input: List<Long>): Either<Error, List<Long>> =
    if (max(pos1, pos2, targetPos) < input.size) {
        val result = if (f(input[pos1], input[pos2])) 1L else 0L
        input.replaceAtIndex(targetPos, result).right()
    } else
        Error("Not able to execute $this on input: out of range").left()

fun JumpOperation.evaluateNewIndex(currentIndex: Int, input: List<Long>): Int =
    if (f(input[pos1])) input[targetPos].toInt()
    else currentIndex + size

fun WriteOutput.executeOn(input: List<Long>, sendChannel: SendChannel<Long>): Either<Error, List<Long>> =
    if (targetPos < input.size) {
        sendChannel.offer(input[targetPos])
        input.right()
    } else Error("Not able to execute $this on input: out of range").left()

fun ReadInput.executeOn(input: List<Long>, receiveChannel: ReceiveChannel<Long>): List<Long> =
    runBlocking { receiveChannel.receive() }
        .let { input.replaceAtIndex(targetPos, it) }

fun ExecutableOperation.executeOn(input: List<Long>): Either<Error, List<Long>> =
    if (max(pos1, pos2, targetPos) < input.size)
        input.replaceAtIndex(targetPos, f(input[pos1], input[pos2])).right()
    else
        Error("Not able to execute $this on input: out of range").left()

fun max(a: Int, b: Int, c: Int) = max(max(a, b), c)

fun CommandTuple<Long>.toOperation(index: Int, base: Int): Either<Error, Operation> {
    val commandDigits = a.toDigitsWithLeadingZeros().map { it.toInt() }
    val (mode3, mode2, mode1, opcode2, opcode1) = commandDigits
    return when {
        opcode1 == 1 && b != null && c != null && d != null ->
            constructThreeArgOperation(mode1, b, mode2, c, mode3, d, index, base, ::Add)
        opcode1 == 2 && b != null && c != null && d != null ->
            constructThreeArgOperation(mode1, b, mode2, c, mode3,  d, index, base, ::Multiply)
        opcode1 == 3 && b != null -> constructReadInputOperation(mode1, b, index, base)
        opcode1 == 4 && b != null -> constructWriteOutputOperation(mode1, b, index, base)
        opcode1 == 5 && b != null && c != null -> constructJumpOperation(mode1, b, mode2, c, index, base, ::JumpIfTrue)
        opcode1 == 6 && b != null && c != null -> constructJumpOperation(mode1, b, mode2, c, index, base, ::JumpIfFalse)
        opcode1 == 7 && b != null && c != null && d != null ->
            constructThreeArgOperation(mode1, b, mode2, c, mode3, d, index, base, ::LessThan)
        opcode1 == 8 && b != null && c != null && d != null ->
            constructThreeArgOperation(mode1, b, mode2, c, mode3, d, index, base, ::Equals)
        opcode1 == 9 && opcode2 == 9 -> Halt.right()
        opcode1 == 9 && b != null -> constructUpdateBaseOperation(mode1, b, index, base)
        else -> Error("Not able to construct command from $this").left()
    }
}

fun constructUpdateBaseOperation(mode1: Int, arg1: Long, index: Int, base: Int): Either<Error, UpdateBase> =
    evaluateArgumentPosition(index + 1, mode1, arg1.toInt(), base)
        .map { UpdateBase(it) }

fun constructWriteOutputOperation(mode1: Int, arg1: Long, index: Int, base: Int): Either<Error, WriteOutput> =
    evaluateArgumentPosition(index + 1, mode1, arg1.toInt(), base)
        .map { WriteOutput(it) }

fun constructReadInputOperation(mode1: Int, arg1: Long, index: Int, base: Int): Either<Error, ReadInput> =
    evaluateArgumentPosition(index + 1, mode1, arg1.toInt(), base)
        .map { ReadInput(it) }

fun constructJumpOperation(
    mode1: Int,
    arg1: Long,
    mode2: Int,
    arg2: Long,
    index: Int,
    base: Int,
    constructor: (Int, Int) -> JumpOperation
): Either<Error, JumpOperation> = Either.fx {
    val (arg1position) = evaluateArgumentPosition(index + 1, mode1, arg1.toInt(), base)
    val (targetCursorPosition) = evaluateArgumentPosition(index + 2, mode2, arg2.toInt(), base)
    constructor(arg1position, targetCursorPosition)
}

// We still gonna use positions for arguments if it's immediate mode, just use index to evaluate position
fun constructThreeArgOperation(
    mode1: Int,
    arg1: Long,
    mode2: Int,
    arg2: Long,
    mode3: Int,
    arg3: Long,
    index: Int,
    base: Int,
    constructor: (Int, Int, Int) -> Operation
): Either<Error, Operation> = Either.fx {
    val (arg1position) = evaluateArgumentPosition(index + 1, mode1, arg1.toInt(), base)
    val (arg2position) = evaluateArgumentPosition(index + 2, mode2, arg2.toInt(), base)
    val (targetPos) = evaluateArgumentPosition(index + 3, mode3, arg3.toInt(), base)
    constructor(arg1position, arg2position, targetPos)
}

fun evaluateArgumentPosition(index: Int, mode: Int, value: Int, base: Int): Either<Error, Int> =
    when (mode) {
        0 -> value.right()
        1 -> index.right()
        2 -> (base + value).right()
        else -> Error("This mode is not supported: $mode").left()
    }
        .flatMap { if(it >= 0) it.right() else Error("Negative target position").left() }

fun Long.toDigitsWithLeadingZeros(): List<Long> {
    val digits = this.digits()
    return if (digits.size < 5) List(5 - digits.size) { 0L } + digits
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

sealed class ExecutableOperation(val f: (Long, Long) -> Long) : Operation() {
    abstract val pos1: Int
    abstract val pos2: Int
    abstract val targetPos: Int
    override val size: Int = 4
}

data class Add(
    override val pos1: Int,
    override val pos2: Int,
    override val targetPos: Int
) : ExecutableOperation(Long::plus)

data class Multiply(
    override val pos1: Int,
    override val pos2: Int,
    override val targetPos: Int
) : ExecutableOperation(Long::times)

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

sealed class JumpOperation(val f: (Long) -> Boolean) : Operation() {
    abstract val pos1: Int
    abstract val targetPos: Int
    override val size: Int = 3
}

data class JumpIfTrue(
    override val pos1: Int,
    override val targetPos: Int
) : JumpOperation({ it != 0L })

data class JumpIfFalse(
    override val pos1: Int,
    override val targetPos: Int
) : JumpOperation({ it == 0L })

sealed class ComparisonOperation(val f: (Long, Long) -> Boolean) : Operation() {
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

data class UpdateBase(val pos1: Int) : Operation() {
    override val size: Int = 2
}

data class CommandTuple<T>(val a: T, val b: T?, val c: T?, val d: T?)