package advent.of.code.day08

import advent.of.code.readInputFromCode

const val width = 25
const val height = 6


fun main() {
    val image = readInputFromCode("08_1.txt")
        .map { it.toString().toInt() }
        .asSequence()
        .chunked(width * height)
        .map { it.mapIndexed { index, layerPixel -> Pair(index, layerPixel) }}
        .flatten()
        .groupBy({ it.first }) { it.second }
        .map {
            it.value.reversed().fold(2) { acc, next ->
                when(next) {
                    1 -> 1
                    0 -> 0
                    else -> acc
                }
            }
        }
        .toList()

    drawImage(image, width)

}

fun drawImage(image: List<Int>, width: Int) = image
    .map { if (it == 0) "0" else " " }
    .chunked(width)
    .forEach { println(it.joinToString("")) }

fun part1() {
    val result = readInputFromCode("08_1.txt")
        .chunked(width * height)
        .minBy { layer -> layer.count { it == '0' } }
        ?.fold(Pair(0, 0)) { acc, next ->
            when(next) {
                '1' -> Pair(acc.first + 1, acc.second)
                '2' -> Pair(acc.first, acc.second + 1)
                else -> acc
            }
        }
        ?.let { it.first * it.second }

    println(result)
}

