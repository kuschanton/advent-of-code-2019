package advent.of.code.day08

fun buildImage(input: String, height: Int, width: Int): List<Int> =
    input.map { it.toString().toInt() }
        .asSequence()
        .chunked(width * height)
        .map { it.mapIndexed { index, layerPixel -> Pair(index, layerPixel) } }
        .flatten()
        .groupBy({ it.first }) { it.second }
        .map {
            it.value.reversed().fold(2) { acc, next ->
                when (next) {
                    1 -> 1
                    0 -> 0
                    else -> acc
                }
            }
        }
        .toList()

fun drawImage(image: List<Int>, width: Int): String =
    image.map { if (it == 0) " " else "X" }
        .chunked(width)
        .joinToString("\n") { it.joinToString("") }

fun findLayer(input: String, height: Int, width: Int): Int? =
    input.chunked(width * height)
        .minBy { layer -> layer.count { it == '0' } }
        ?.fold(Pair(0, 0)) { acc, next ->
            when (next) {
                '1' -> Pair(acc.first + 1, acc.second)
                '2' -> Pair(acc.first, acc.second + 1)
                else -> acc
            }
        }
        ?.let { it.first * it.second }

