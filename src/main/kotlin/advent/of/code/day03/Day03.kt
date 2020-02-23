package advent.of.code.day03

import advent.of.code.Error
import advent.of.code.eitherCatch
import advent.of.code.toEitherList
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.firstOrNone
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import kotlin.math.abs


fun findShortestCircuit(input1: String, input2: String): Either<Error, Int> = Either.fx {
    val (wire1) = input1.toWire()
    val (wire2) = input2.toWire()
    findShortestCircuit(wire1, wire2).bind()
}

private fun findShortestCircuit(wire1: List<Direction>, wire2: List<Direction>): Either<Error, Int> =
    findWiresIntersections(wire1, wire2)
        .firstOrNone()
        .toEither { Error("Wires do not intersect") }
        .map { wire1.toPoints().indexOf(it) + wire2.toPoints().indexOf(it) }

private fun findWiresIntersections(wire1: List<Direction>, wire2: List<Direction>): List<Point> =
    wire1.toPoints().toSet()
        .intersect(wire2.toPoints().toSet())
        .filter { it != Point(0, 0) }

fun findClosestIntersection(input1: String, input2: String): Either<Error, Int> = Either.fx {
    val (wire1) = input1.toWire()
    val (wire2) = input2.toWire()
    val (closestIntersection) = findClosestIntersection(wire1, wire2)
    closestIntersection.manhattanDistance()
}

private fun Point.manhattanDistance(): Int = abs(x) + abs(y)

private fun findClosestIntersection(wire1: List<Direction>, wire2: List<Direction>): Either<Error, Point> =
    findWiresIntersections(wire1, wire2)
        .minBy { abs(it.x) + abs(it.y) }
        .toOption()
        .toEither { Error("Wires do not intersect") }

private fun List<Direction>.toPoints(): List<Point> =
    this.fold(listOf(Point(0, 0))) { acc, next ->
        val continueFrom = acc.last()
        acc + next.toPoints(continueFrom).drop(1)
    }

private fun Direction.toPoints(start: Point): List<Point> =
    when (this) {
        is R -> (start.x..(start.x + this.steps)).map {
            Point(
                it,
                start.y
            )
        }
        is L -> (start.x downTo (start.x - this.steps)).map {
            Point(
                it,
                start.y
            )
        }
        is U -> (start.y..(start.y + this.steps)).map {
            Point(
                start.x,
                it
            )
        }
        is D -> (start.y downTo (start.y - this.steps)).map {
            Point(
                start.x,
                it
            )
        }
    }

private fun String.toWire(): Either<Error, List<Direction>> =
    this.split(',')
        .map { it.toDirection() }
        .toEitherList()

private fun String.toDirection(): Either<Error, Direction> =
    if (this.isEmpty()) Error("Leg is empty").left()
    else parseLeg(this.first(), this.drop(1))

private fun parseLeg(direction: Char, steps: String): Either<Error, Direction> = Either.fx {
    val (directionConstructor) = parseDirection(direction)
    val (stepsNumber) = steps.toIntOrError()
    directionConstructor(stepsNumber)
}

private fun parseDirection(direction: Char): Either<Error, (Int) -> Direction> =
    when {
        direction.equals('R', true) -> ::R.right()
        direction.equals('L', true) -> ::L.right()
        direction.equals('U', true) -> ::U.right()
        direction.equals('D', true) -> ::D.right()
        else -> Error("Not able to parse direction from $direction").left()
    }

private fun String.toIntOrError(): Either<Error, Int> =
    eitherCatch { this.toInt() }
        .mapLeft { Error("Not able to parse Int from $this") }

data class Point(val x: Int, val y: Int)

sealed class Direction

data class R(val steps: Int) : Direction()
data class L(val steps: Int) : Direction()
data class U(val steps: Int) : Direction()
data class D(val steps: Int) : Direction()
