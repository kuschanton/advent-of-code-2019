package advent.of.code.day01

import kotlin.math.max

// Part 1
fun fuelForModules(input: List<Int>): Int = input
    .asSequence()
    .map { fuelForMass(it) }
    .sum()

// Part 2
fun fuelForModulesAndFuel(input: List<Int>): Int = input
    .asSequence()
    .map { fuelForMass(it) }
    .map { it + fuelForFuel(it) }
    .sum()

fun fuelForMass(mass: Int): Int = (mass.toDouble() / 3)
    .toInt()
    .let { it - 2 }
    .let { max(it, 0) }

private fun fuelForFuel(fuelMass: Int): Int {
    tailrec fun go(current: Int, total: Int): Int {
        val fuelRequired = fuelForMass(current)
        return when {
            fuelRequired <= 0 -> total
            else -> go(fuelRequired, fuelRequired + total)
        }
    }
    return go(fuelMass, 0)
}