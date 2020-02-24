package advent.of.code.day06

import advent.of.code.toEitherList
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.right
import arrow.core.toOption

private const val COM = "COM"

fun findPath(input: String) = Either.fx<Error, Int> {
        val (directOrbits) = parseInputPairs(input)
        val pathToYou = findPathTo("YOU", directOrbits)
        val pathToSanta = findPathTo("SAN", directOrbits)
        val (lastCommon) = pathToYou.zip(pathToSanta)
            .last { it.first == it.second }
            .first
            .toOption().toEither { Error("No common elements") }
        val yourPathToLastCommon = pathToYou.size - pathToYou.indexOf(lastCommon) - 1
        val santaPathToLastCommon = pathToSanta.size - pathToYou.indexOf(lastCommon) - 1
        yourPathToLastCommon + santaPathToLastCommon
    }

private fun findPathTo(targetName: String, list: List<Pair<String, String>>): List<String> {
    tailrec fun go(name: String, path: List<String>, list: List<Pair<String, String>>): List<String> {
        val next = list.singleOrNull { it.second == name }
        return when {
            next == null -> emptyList()
            next.first == COM -> (listOf(COM) + path)
            else -> go(next.first, listOf(next.first) + path, list)
        }
    }
    return go(targetName, emptyList(), list)
}

fun countOrbits(input: String) = Either.fx<Error, Int> {
    val (directOrbits) = parseInput(input)
    val tree = buildObject(directOrbits)
    tree.countInterconnections(1)
}

private fun SpaceTree.countInterconnections(depth: Int): Int =
    when {
        left != null && right != null -> left.countInterconnections(depth + 1) + right.countInterconnections(depth + 1) - depth * (depth - 1) / 2
        left != null || right != null -> (left ?: right)!!.countInterconnections(depth + 1)
        else -> depth * (depth - 1) / 2
    }

private fun parseInputPairs(input: String): Either<Error, List<Pair<String, String>>> =
    input.lines()
        .map { it.toPair() }
        .toEitherList()

private fun parseInput(input: String): Either<Error, Map<String, Set<String>>> =
    parseInputPairs(input)
        .map {
            it.fold(mutableMapOf<String, Set<String>>()) { acc, next ->
                acc.apply {
                    compute(next.first) { _, value ->
                        (value ?: emptySet()) + next.second
                    }
                }
            }
        }

private fun buildObject(inputMap: Map<String, Set<String>>): SpaceTree {
    fun go(name: String, map: Map<String, Set<String>>): SpaceTree {
        val childrenNames = map[name]
        return when {
            childrenNames.isNullOrEmpty() -> SpaceTree(name)
            childrenNames.size == 1 -> SpaceTree(name, go(childrenNames.single(), map))
            else -> {
                val (left, right) = childrenNames.toList()
                SpaceTree(name, go(left, map), go(right, map))
            }
        }
    }
    return go(COM, inputMap)
}

private fun String.toPair(): Either<Error, Pair<String, String>> {
    val split = this.split(')')
    return when (split.size) {
        2 -> Pair(split.component1(), split.component2()).right()
        else -> Error("Unparseable input $this").left()
    }
}

private data class SpaceTree(val name: String, val left: SpaceTree? = null, val right: SpaceTree? = null)

