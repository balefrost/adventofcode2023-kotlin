package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class Day24 {
    val sampleText = """
        19, 13, 30 @ -2,  1, -2
        18, 19, 22 @ -1, -1, -2
        20, 25, 34 @ -2, -2, -4
        12, 31, 28 @ -1, -2, -1
        20, 19, 15 @  1, -5, -3
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day24.txt")!!.readText()

    //    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Hail(val pos: DPoint3D, val dir: DDir3D)

    val lineRegex = """([-\d]+), +([-\d]+), +([-\d]+) @ +([-\d]+), +([-\d]+), +([-\d]+)""".toRegex()

    val input = lines.map { line ->
        val values = lineRegex.matchEntire(line)!!.groupValues
        val numbers = values.drop(1).map { it.toDouble() }
        val (x, y, z) = numbers
        val (dx, dy, dz) = numbers.drop(3)
        Hail(DPoint3D(x, y, z), DDir3D(dx, dy, dz))
    }

    //    val bounds = DRegion2D(7.0..27.0, 7.0..27.0)
    val bounds = DRegion2D(
        200000000000000.0..400000000000000.0,
        200000000000000.0..400000000000000.0
    )

    fun <T> List<T>.pairs() = sequence {
        for (aIdx in this@pairs.indices) {
            val a = this@pairs[aIdx]
            for (bIdx in aIdx + 1..this@pairs.lastIndex) {
                val b = this@pairs[bIdx]
                yield(a to b)
            }
        }
    }

    @Test
    fun part01() {
        val colliding = input.pairs()
            .mapNotNull { (a, b) ->
                val parametricIntersection = gaussianElim(
                    listOf(
                        listOf(a.dir.dx, -b.dir.dx, a.pos.x - b.pos.x),
                        listOf(a.dir.dy, -b.dir.dy, a.pos.y - b.pos.y)
                    )
                )
                if (parametricIntersection != null) {
                    val (t1, t2) = parametricIntersection
                    if (t1 < 0 || t2 < 0) {
                        null
                    } else {
                        a.pos.xy + a.dir.xy * parametricIntersection[0]
                    }
                } else null
            }.count { it in bounds }

        println(colliding)
    }

    @Test
    fun part02a() {
    }

}