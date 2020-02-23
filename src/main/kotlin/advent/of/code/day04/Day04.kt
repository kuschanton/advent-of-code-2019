package advent.of.code.day04

import advent.of.code.digits
import advent.of.code.replaceLast
import arrow.core.extensions.list.foldable.fold
import arrow.core.k
import arrow.typeclasses.Monoid

fun findPassword(rangeStart: Int, rangeEnd: Int) = (rangeStart..rangeEnd)
        .map { it.digits() }
        .filter { combinedRules(it) }
//      Alternative
//      .filter { rules.fold(true) { acc, rule -> acc && rule(it) } }
        .count()

private val twoAdjacentSame: (List<Int>) -> Boolean = {
    it.fold(Pair(0, false)) { acc, value ->
        Pair(value, acc.second || acc.first == value)
    }.second
}

private val neverDecrease: (List<Int>) -> Boolean = {
    it.fold(Pair(0, true)) { acc, value ->
        Pair(value, acc.second && acc.first <= value)
    }.second
}

private val sameAreNotInLargerGroup: (List<Int>) -> Boolean = {
    it.toSequences()
        .any { sequence -> sequence.size == 2 }
}

private fun <E> List<E>.toSequences(): List<List<E>> {
    val pairs = this.fold(emptyList<Pair<E, Int>>()) { acc, value ->
        if (acc.isNotEmpty() && acc.last().first == value) acc.replaceLast(Pair(value, acc.last().second + 1))
        else acc + Pair(value, 1)
    }
    return pairs.map { List(it.second) { _ -> it.first} }
}

private val rules = listOf(
    twoAdjacentSame,
    neverDecrease,
    sameAreNotInLargerGroup
)

val monoid = object : Monoid<(List<Int>) -> Boolean> {
    override fun empty(): (List<Int>) -> Boolean = { true }

    override fun ((List<Int>) -> Boolean).combine(b: (List<Int>) -> Boolean): (List<Int>) -> Boolean = {
        this(it) && b(it)
    }
}

private val combinedRules = rules.k().fold(monoid)
