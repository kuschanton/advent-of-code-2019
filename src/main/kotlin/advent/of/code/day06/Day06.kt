package advent.of.code.day06

import advent.of.code.day03.toEitherList
import advent.of.code.readInputFrom
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right

val testInput = """
COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L
""".trimIndent()

val testInput2 = """
COM)B
B)G
G)H
B)C
C)D
D)I
""".trimIndent()

fun main() {
    val result = Either.fx<Error, Int> {
        val (directOrbits) = parseInput(readInputFrom("06.txt"))
        val tree = buildObject(directOrbits)
        tree.countInterconnections(1)
    }
    println(result)
}

fun SpaceObject.countInterconnections(depth: Int): Int =
    when {
        left != null && right != null -> left.countInterconnections(depth + 1) + right.countInterconnections(depth + 1) - depth * (depth - 1) / 2
        left != null || right != null -> (left ?: right)!!.countInterconnections(depth + 1)
        else -> depth * (depth - 1) / 2
    }

fun parseInput(input: String): Either<Error, Map<String, Set<String>>> =
    input.lines()
        .map { it.toPair() }
        .toEitherList()
        .map {
            it.fold(mutableMapOf<String, Set<String>>()) { acc, next ->
                acc.apply {
                    compute(next.first) { _, value ->
                        (value ?: emptySet()) + next.second
                    }
                }
            }
        }

fun buildObject(inputMap: Map<String, Set<String>>): SpaceObject {
    fun go(name: String, map: Map<String, Set<String>>): SpaceObject {
        val childrenNames = map[name]
        return when {
            childrenNames.isNullOrEmpty() -> SpaceObject(name)
            childrenNames.size == 1 -> SpaceObject(name, go(childrenNames.single(), map))
            else -> {
                val (left, right) = childrenNames.toList()
                SpaceObject(name, go(left, map), go(right, map))
            }
        }
    }
    return go("COM", inputMap)
}

fun String.toPair(): Either<Error, Pair<String, String>> {
    val split = this.split(')')
    return when (split.size) {
        2 -> Pair(split.component1(), split.component2()).right()
        else -> Error("Unparseable input $this").left()
    }
}

data class SpaceObject(val name: String, val left: SpaceObject? = null, val right: SpaceObject? = null)

