package advent.of.code

import arrow.core.left
import arrow.core.right

data class Error(val message: String)

fun <T> eitherCatch(block: () -> T) = runCatching { block().right() }.getOrElse { it.left() }

fun delimiter() = println("_".repeat(20))

fun <T> List<T>.replaceAtIndex(index: Int, newValue: T) = this.mapIndexed { i, v ->
    if (index == i) newValue else v
}

fun <T> List<T>.replaceLast(newValue: T) = replaceAtIndex(size - 1, newValue)

fun Int.digits(): List<Int> {
    fun go(value: Int, digits: List<Int>): List<Int> =
        when {
            value > 0 -> go(value / 10, listOf(value % 10) + digits)
            else -> digits
        }
    return go(this, emptyList())
}

