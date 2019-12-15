package advent.of.code.day07

import advent.of.code.Error
import advent.of.code.day03.toEitherList
import advent.of.code.digits
import advent.of.code.eitherCatch
import advent.of.code.readInputFrom
import advent.of.code.replaceAtIndex
import arrow.core.*
import arrow.core.extensions.fx
import com.marcinmoskala.math.permutations
import java.lang.Integer.max
import java.util.Scanner
import java.util.Stack


fun main() {
    part1()
}

fun part1(): Unit {
    val result = Either.fx<Error, Int> {
        val input = readInputFrom("07_1.txt")
            .split(',')
            .map { eitherCatch { it.toInt() } }
            .toEitherList()
            .mapLeft { ex -> Error("Not able to convert to Int: $ex") }
            .bind()

        (0..4).toSet().permutations().fold(0) { acc, next ->
            max(acc, executePermutation(input, next).bind())
        }
    }
}

fun executePermutation(input: List<Int>, permutation: List<Int>): Either<Error, Int> =
    permutation.fold<Int, Either<Error, Int>>(0.right()) { acc, next ->
        Either.fx {
            val inputStack = Stack<Int>().apply { push(acc.bind()); push(next) }
            val (output) = executeOperations(input, inputStack)
            output.singleOrError().bind()
        }
    }

private fun <E> List<E>.singleOrError(): Either<Error, E> =
    if (size == 1) single().right()
    else Error("Unexpected output size $size").left()

fun executeOperations(input: List<Int>, inputStack: Stack<Int>): Either<Error, List<Int>> {
    val output = mutableListOf<Int>()

    fun go(index: Int, input: List<Int>): Either<Error, List<Int>> = Either.fx {
        val (commandTuple) = input.nextFourFrom(index)
        val (command) = commandTuple.toOperation(index)
        when (command) {
            is Halt -> input.right()
            is ExecutableOperation -> go(index + command.size, command.executeOn(input).bind())
            is ReadInput -> go(index + command.size, command.executeOn(input, inputStack).bind())
            is WriteOutput -> go(index + command.size, command.executeOn(input, output).bind())
            is JumpIfTrue -> go(command.evaluateNewIndex(index, input), input)
            is JumpIfFalse -> go(command.evaluateNewIndex(index, input), input)
            is LessThan -> go(index + command.size, command.executeOn(input).bind())
            is Equals -> go(index + command.size, command.executeOn(input).bind())
        }.bind()
    }

    return go(0, input).map { output }
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

fun WriteOutput.executeOn(input: List<Int>, output: MutableList<Int>): Either<Error, List<Int>> =
    if (targetPos < input.size) {
        output.add(input[targetPos])
        input.right()
    } else Error("Not able to execute $this on input: out of range").left()

fun ReadInput.executeOn(input: List<Int>, stackInput: Stack<Int>): Either<Error, List<Int>> =
    if (targetPos < input.size)
        stackInput.pop().toOption().toEither { Error("Input is empty") }
            .map { input.replaceAtIndex(targetPos, it) }
    else
        Error("Not able to execute $this on input: out of range").left()

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

val taskInput = listOf(
    3,
    225,
    1,
    225,
    6,
    6,
    1100,
    1,
    238,
    225,
    104,
    0,
    1002,
    148,
    28,
    224,
    1001,
    224,
    -672,
    224,
    4,
    224,
    1002,
    223,
    8,
    223,
    101,
    3,
    224,
    224,
    1,
    224,
    223,
    223,
    1102,
    8,
    21,
    225,
    1102,
    13,
    10,
    225,
    1102,
    21,
    10,
    225,
    1102,
    6,
    14,
    225,
    1102,
    94,
    17,
    225,
    1,
    40,
    173,
    224,
    1001,
    224,
    -90,
    224,
    4,
    224,
    102,
    8,
    223,
    223,
    1001,
    224,
    4,
    224,
    1,
    224,
    223,
    223,
    2,
    35,
    44,
    224,
    101,
    -80,
    224,
    224,
    4,
    224,
    102,
    8,
    223,
    223,
    101,
    6,
    224,
    224,
    1,
    223,
    224,
    223,
    1101,
    26,
    94,
    224,
    101,
    -120,
    224,
    224,
    4,
    224,
    102,
    8,
    223,
    223,
    1001,
    224,
    7,
    224,
    1,
    224,
    223,
    223,
    1001,
    52,
    70,
    224,
    101,
    -87,
    224,
    224,
    4,
    224,
    1002,
    223,
    8,
    223,
    1001,
    224,
    2,
    224,
    1,
    223,
    224,
    223,
    1101,
    16,
    92,
    225,
    1101,
    59,
    24,
    225,
    102,
    83,
    48,
    224,
    101,
    -1162,
    224,
    224,
    4,
    224,
    102,
    8,
    223,
    223,
    101,
    4,
    224,
    224,
    1,
    223,
    224,
    223,
    1101,
    80,
    10,
    225,
    101,
    5,
    143,
    224,
    1001,
    224,
    -21,
    224,
    4,
    224,
    1002,
    223,
    8,
    223,
    1001,
    224,
    6,
    224,
    1,
    223,
    224,
    223,
    1102,
    94,
    67,
    224,
    101,
    -6298,
    224,
    224,
    4,
    224,
    102,
    8,
    223,
    223,
    1001,
    224,
    3,
    224,
    1,
    224,
    223,
    223,
    4,
    223,
    99,
    0,
    0,
    0,
    677,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    0,
    1105,
    0,
    99999,
    1105,
    227,
    247,
    1105,
    1,
    99999,
    1005,
    227,
    99999,
    1005,
    0,
    256,
    1105,
    1,
    99999,
    1106,
    227,
    99999,
    1106,
    0,
    265,
    1105,
    1,
    99999,
    1006,
    0,
    99999,
    1006,
    227,
    274,
    1105,
    1,
    99999,
    1105,
    1,
    280,
    1105,
    1,
    99999,
    1,
    225,
    225,
    225,
    1101,
    294,
    0,
    0,
    105,
    1,
    0,
    1105,
    1,
    99999,
    1106,
    0,
    300,
    1105,
    1,
    99999,
    1,
    225,
    225,
    225,
    1101,
    314,
    0,
    0,
    106,
    0,
    0,
    1105,
    1,
    99999,
    108,
    677,
    677,
    224,
    102,
    2,
    223,
    223,
    1005,
    224,
    329,
    101,
    1,
    223,
    223,
    1107,
    677,
    226,
    224,
    102,
    2,
    223,
    223,
    1006,
    224,
    344,
    101,
    1,
    223,
    223,
    1107,
    226,
    226,
    224,
    102,
    2,
    223,
    223,
    1006,
    224,
    359,
    101,
    1,
    223,
    223,
    1108,
    677,
    677,
    224,
    102,
    2,
    223,
    223,
    1005,
    224,
    374,
    101,
    1,
    223,
    223,
    8,
    677,
    226,
    224,
    1002,
    223,
    2,
    223,
    1005,
    224,
    389,
    101,
    1,
    223,
    223,
    108,
    226,
    677,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    404,
    1001,
    223,
    1,
    223,
    107,
    677,
    677,
    224,
    102,
    2,
    223,
    223,
    1006,
    224,
    419,
    101,
    1,
    223,
    223,
    1007,
    226,
    226,
    224,
    102,
    2,
    223,
    223,
    1005,
    224,
    434,
    101,
    1,
    223,
    223,
    1007,
    677,
    677,
    224,
    102,
    2,
    223,
    223,
    1005,
    224,
    449,
    1001,
    223,
    1,
    223,
    8,
    677,
    677,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    464,
    101,
    1,
    223,
    223,
    1108,
    677,
    226,
    224,
    1002,
    223,
    2,
    223,
    1005,
    224,
    479,
    101,
    1,
    223,
    223,
    7,
    677,
    226,
    224,
    1002,
    223,
    2,
    223,
    1005,
    224,
    494,
    101,
    1,
    223,
    223,
    1008,
    677,
    677,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    509,
    1001,
    223,
    1,
    223,
    1007,
    226,
    677,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    524,
    1001,
    223,
    1,
    223,
    107,
    226,
    226,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    539,
    1001,
    223,
    1,
    223,
    1107,
    226,
    677,
    224,
    102,
    2,
    223,
    223,
    1005,
    224,
    554,
    101,
    1,
    223,
    223,
    1108,
    226,
    677,
    224,
    102,
    2,
    223,
    223,
    1006,
    224,
    569,
    101,
    1,
    223,
    223,
    108,
    226,
    226,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    584,
    1001,
    223,
    1,
    223,
    7,
    226,
    226,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    599,
    101,
    1,
    223,
    223,
    8,
    226,
    677,
    224,
    102,
    2,
    223,
    223,
    1005,
    224,
    614,
    101,
    1,
    223,
    223,
    7,
    226,
    677,
    224,
    1002,
    223,
    2,
    223,
    1005,
    224,
    629,
    101,
    1,
    223,
    223,
    1008,
    226,
    677,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    644,
    101,
    1,
    223,
    223,
    107,
    226,
    677,
    224,
    1002,
    223,
    2,
    223,
    1005,
    224,
    659,
    1001,
    223,
    1,
    223,
    1008,
    226,
    226,
    224,
    1002,
    223,
    2,
    223,
    1006,
    224,
    674,
    1001,
    223,
    1,
    223,
    4,
    223,
    99,
    226
)