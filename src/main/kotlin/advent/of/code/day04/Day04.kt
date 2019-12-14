package advent.of.code.day04

import advent.of.code.digits
import advent.of.code.replaceLast
import arrow.core.extensions.list.foldable.fold
import arrow.core.extensions.monoid
import arrow.core.k
import arrow.typeclasses.Monoid

val rangeStart = 367479
val rangeEnd = 893698

fun main() {
    val combinedRules = rules.k().fold(monoid)

    val result = (rangeStart..rangeEnd)
        .map { it.digits() }
//        .filter { combinedRules(it) }
        .filter { rules.fold(true) { acc, rule -> acc && rule(it) } }
        .count()
    println(result)
    String.monoid()
}

val monoid = object : Monoid<(List<Int>) -> Boolean> {
    override fun empty(): (List<Int>) -> Boolean = { true }

    override fun ((List<Int>) -> Boolean).combine(b: (List<Int>) -> Boolean): (List<Int>) -> Boolean = {
        this(it) && b(it)
    }

}

val twoAdjacentSame: (List<Int>) -> Boolean = {
    it.fold(Pair(0, false)) { acc, value ->
        Pair(value, acc.second || acc.first == value)
    }.second
}

val neverDecrease: (List<Int>) -> Boolean = {
    it.fold(Pair(0, true)) { acc, value ->
        Pair(value, acc.second && acc.first <= value)
    }.second
}

val sameAreNotInLargerGroup: (List<Int>) -> Boolean = {
    it.toSequences()
        .any { sequence -> sequence.size == 2 }
}

fun <E> List<E>.toSequences(): List<List<E>> {
    val pairs = this.fold(emptyList<Pair<E, Int>>()) { acc, value ->
        if (acc.isNotEmpty() && acc.last().first == value) acc.replaceLast(Pair(value, acc.last().second + 1))
        else acc + Pair(value, 1)
    }
    return pairs.map { List(it.second) { _ -> it.first} }
}

fun MatchResult.lengthIsUpto(maxInclusive: Int): Boolean =
    this.groupValues.single().length <= 2

val rules = listOf(
    twoAdjacentSame,
    neverDecrease,
    sameAreNotInLargerGroup
)
