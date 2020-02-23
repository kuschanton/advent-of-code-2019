package advent.of.code

import arrow.core.Either
import arrow.core.extensions.either.applicative.applicative
import arrow.core.extensions.list.traverse.sequence
import arrow.core.fix
import arrow.core.left
import arrow.core.right
import java.io.File

data class Error(val message: String)

fun <E, T> List<Either<E, T>>.toEitherList(): Either<E, List<T>> =
    sequence(Either.applicative())
        .fix()
        .map { it.fix() }

fun <T> eitherCatch(block: () -> T): Either<Throwable, T> = runCatching { block().right() }.getOrElse { it.left() }

fun <A, B, E> List<Either<E, A>>.filterMap(block: (A) -> B): List<B> =
    flatMap { it.fold({ emptyList<B>() }, { right -> listOf(block(right)) }) }

fun <L, R> List<Either<L, R>>.filterRight(block: (R) -> Boolean): List<R> =
    flatMap {
        it.fold(
            { emptyList<R>() },
            { right -> if (block(right)) listOf(right) else emptyList() }
        )
    }

fun <T> List<T>.replaceAtIndex(index: Int, newValue: T) = this.mapIndexed { i, v ->
    if (index == i) newValue else v
}

fun <T> List<List<T>>.replaceAtIndex2(indexX: Int, indexY: Int, newValue: T) = this.replaceAtIndex(
    indexY, this[indexY].replaceAtIndex(indexX, newValue)
)

fun <T> List<T>.replaceLast(newValue: T) = replaceAtIndex(size - 1, newValue)

fun Int.digits(): List<Int> {
    tailrec fun go(value: Int, digits: List<Int>): List<Int> =
        when {
            value > 0 -> go(value / 10, listOf(value % 10) + digits)
            else -> digits
        }
    return go(this, emptyList())
}

fun Long.digits(): List<Long> {
    tailrec fun go(value: Long, digits: List<Long>): List<Long> =
        when {
            value > 0 -> go(value / 10, listOf(value % 10) + digits)
            else -> digits
        }
    return go(this, emptyList())
}

fun readInputFromCode(fileName: String) = File("src/main/resources/task_input/$fileName").readText()

