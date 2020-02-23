package advent.of.code.day09

import advent.of.code.Error
import advent.of.code.day03.toEitherList
import advent.of.code.digits
import advent.of.code.eitherCatch
import advent.of.code.readInputFrom
import advent.of.code.replaceAtIndex
import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.extensions.id.applicative.just
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import java.lang.Integer.max


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