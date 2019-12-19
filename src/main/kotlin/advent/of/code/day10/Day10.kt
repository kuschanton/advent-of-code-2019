package advent.of.code.day10

import advent.of.code.readInputFrom
import arrow.core.Tuple4
import arrow.core.Tuple6
import arrow.core.Tuple7
import com.marcinmoskala.math.pow
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

fun main() {


    val startingPoint = Pair(17, 22)
    val asteroids = readInputFrom("10_1.txt")
        .toAsteroids() - (startingPoint)
    println(asteroids.orderByVaporization(startingPoint)[199])

}

fun List<Pair<Int, Int>>.orderByVaporization(centre: Pair<Int, Int>): List<Tuple7<Int, Int, Int, Int, Double, Double, Int>> {
    val withCircle = this
        .map {
            val relativePosition = it.toRelativePositionFrom(centre)
            Tuple4(it.first, it.second, relativePosition.first, relativePosition.second)
        }
        .map {
            val distance = sqrt(it.c.pow(2) + it.d.pow(2).toDouble())
            val angle = (it.c to it.d).toAngle()
            Tuple6(it.a, it.b, it.c, it.d, distance, "%.4f".format(angle).toDouble())
        }
        .groupBy { it.f }
        .map { (_, value) ->
            value.sortedBy { it.e }.mapIndexed { index, tuple6 ->
                Tuple7(tuple6.a, tuple6.b, tuple6.c, tuple6.d, tuple6.e, tuple6.f, index)
            }
        }
        .flatten()
    val circles = withCircle.map { it.g }.max() ?: 0
    return (0..circles).fold<Int, List<Tuple7<Int, Int, Int, Int, Double, Double, Int>>>(emptyList()) { acc, next ->
        acc + withCircle.filter { it.g == next }
            .sortedBy { it.f }
    }
}

private fun Pair<Int, Int>.toAngle(): Double =
    Math.toDegrees(atan2(-first.toDouble(), second.toDouble())) + 180

private fun Pair<Int, Int>.foldByAxis(): Pair<Int, Int> =
    when {
        first == 0 -> Pair(0, second.toOne())
        second == 0 -> Pair(first.toOne(), 0)
        else -> this
    }

fun part1() {
    val asteroids = readInputFrom("10_1.txt")
        .toAsteroids()

    val result = asteroids.fold(Triple(0, 0, 0)) { acc, pair ->
        val visible = pair.countVisible(asteroids)
        if (visible > acc.first) Triple(visible, pair.first, pair.second)
        else acc
    }

    println(result)
}

private fun Pair<Int, Int>.countVisible(asteroids: List<Pair<Int, Int>>): Int =
    (asteroids - this)
        .map { it.toRelativePositionFrom(this) }
        .map { it.reduce() }
        .foldOnSameAxis()
        .toSet()
        .count()

private fun List<Pair<Int, Int>>.foldOnSameAxis(): List<Pair<Int, Int>> =
    this.fold(emptyList()) { acc, next ->
        when {
            next.first == 0 -> acc + Pair(0, next.second.toOne())
            next.second == 0 -> acc + Pair(next.first.toOne(), 0)
            else -> acc + next
        }
    }

private fun Int.toOne(): Int = when {
    this < 0 -> -1
    this == 0 -> 0
    else -> 1
}

private fun Pair<Int, Int>.reduce(): Pair<Int, Int> =
    if (first == 0 || second == 0) this
    else {
        val gcd = findGcd(abs(first), abs(second))
        Pair(first / gcd, second / gcd)
    }

fun findGcd(first: Int, second: Int): Int {
    fun go(a: Int, b: Int): Int {
        val res = max(a, b) % min(a, b)
        return if (res == 0) min(a, b)
        else go(res, min(a, b))
    }
    return go(first, second)
}

private fun Pair<Int, Int>.toRelativePositionFrom(centre: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first - centre.first, second - centre.second)

fun String.toAsteroids(): List<Pair<Int, Int>> =
    this.lines().foldIndexed(emptyList()) { line, acc, next ->
        acc + next.foldIndexed(emptyList<Pair<Int, Int>>()) { row, innerAcc, char ->
            when (char) {
                '.' -> innerAcc
                else -> innerAcc + Pair(row, line)
            }
        }
    }

