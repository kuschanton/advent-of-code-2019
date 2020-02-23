package advent.of.code.day04

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class Day04Test {

    @Test
    internal fun test1() {
        expectThat(findPassword(367479, 893698))
            .isEqualTo(305)
    }
}