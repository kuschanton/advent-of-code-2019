package advent.of.code.day09

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.left
import arrow.core.right


fun main() {
    val sdf = Either.tailRecM(Triple(0, 1, 5)) {
        println(it.second)
        val asd = Either.right(
            if (it.third > 0) Triple(it.second, it.first + it.second, it.third - 1).left()
            else it.first.right()
        )
        val dfg = Either.fx<Nothing, Either<Triple<Int, Int, Int>, Int>> {
            if (it.third > 0) Triple(it.second, it.first + it.second, it.third - 1).left()
            else it.first.right()
        }
        dfg
    }
    println(sdf)
}