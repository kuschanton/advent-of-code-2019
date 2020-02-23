package advent.of.code.day12

import advent.of.code.readInputFrom
import java.time.LocalDateTime
import kotlin.math.abs

val testInput = """
    <x=-1, y=0, z=2>
    <x=2, y=-10, z=-7>
    <x=4, y=-8, z=8>
    <x=3, y=5, z=-1>
""".trimIndent()

val testInput2 = """
    <x=-8, y=-10, z=0>
    <x=5, y=5, z=10>
    <x=2, y=-7, z=3>
    <x=9, y=-8, z=-3>
""".trimIndent()

val testInput3 = """
<x=-1, y=0, z=2>
<x=2, y=-10, z=-7>
<x=4, y=-8, z=8>
<x=3, y=5, z=-1>
""".trimIndent()

fun main() {
    val moonsPositions = readInputFrom("12_1.txt").parseInput()

    val initialState: List<Moon> = moonsPositions.map { it to Triple(0, 0, 0) }

    // Part 1
    val result = applySteps(1000, initialState)
    result.forEach { println(it) }

    val energy = result.map { moon ->
        moon.first.toList().map { abs(it) }.sum() to moon.second.toList().map { abs(it) }.sum()
    }.map { it.first * it.second }
        .sum()

    println(energy)

    // Part 2
    println(countSteps(initialState))

}

fun String.parseInput(): List<Triple<Int, Int, Int>> = replace('<', ' ')
    .replace('>', ' ')
    .lines()
    .map { it.split(',') }
    .map { line -> line.map { it.trim().split('=')[1].toInt() } }
    .map { line ->
        val (x, y, z) = line
        Triple(x, y, z)
    }

fun applySteps(steps: Int, initialState: List<Moon>): List<Moon> {
    tailrec fun go(remainingSteps: Int, state: List<Moon>): List<Moon> {
        return when {
            remainingSteps <= 0 -> state
            else -> {
                go(remainingSteps - 1, state.nextStep())
            }
        }

    }

    return go(steps, initialState)
}

fun powersOfTen() = generateSequence(10L) { it * 10 }

fun countSteps(initialState: List<Moon>): Long {
    val initialPositions = initialState.map { it.first }
    tailrec fun go(step: Long, state: List<Moon>): Long {
        if (step in powersOfTen().take(10).toList()) { println("${LocalDateTime.now()} $step") }
        return when {
            state.map { it.first } == initialPositions && step != 0L -> step
            else -> go(step + 1, state.nextStep())
        }
    }

    return go(0, initialState)
}

fun List<Moon>.nextStep(): List<Moon> = this.map {
    it.applyVelocitiesFrom(this - it).updateCoordinates()
}

fun Moon.applyVelocitiesFrom(others: List<Moon>): Moon = others.fold(this) { acc, next ->
    acc.applyVelocityFrom(next)
}

fun Moon.updateCoordinates() = Pair(
    Triple(posX + velX, posY + velY, posZ + velZ),
    this.second
)

fun Moon.applyVelocityFrom(other: Moon): Moon {
    val (velXDiff, velYDiff, velZDiff) = velocityAdjustmentFrom(other)
    return Pair(
        this.first,
        Triple(velX + velXDiff, velY + velYDiff, velZ + velZDiff)
    )
}

fun Moon.velocityAdjustmentFrom(other: Moon) =
    Triple(
        this.posX.velocityAdjustmentFrom(other.posX),
        this.posY.velocityAdjustmentFrom(other.posY),
        this.posZ.velocityAdjustmentFrom(other.posZ)
    )

fun Int.velocityAdjustmentFrom(other: Int) = when {
    this < other -> 1
    this == other -> 0
    else -> -1
}

typealias Moon = Pair<Triple<Int, Int, Int>, Triple<Int, Int, Int>>

val Moon.posX: Int
    get() = first.first
val Moon.posY: Int
    get() = first.second
val Moon.posZ: Int
    get() = first.third
val Moon.velX: Int
    get() = second.first
val Moon.velY: Int
    get() = second.second
val Moon.velZ: Int
    get() = second.third
