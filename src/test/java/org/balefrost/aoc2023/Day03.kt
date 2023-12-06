package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day03 {
    val sampleText = """
        467..114..
        ...*......
        ..35..633.
        ......#...
        617*......
        .....+.58.
        ..592.....
        ......755.
        ...${'$'}.*....
        .664.598..
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day03.txt")!!.readText()
//    val inputText = sampleText
    val inputText = textFromFile
    val lines = inputText.lines().filterNot { it.isEmpty() }

    data class Point2D(val x: Int, val y: Int)
    data class Region2D(val x: Int, val y: Int, val w: Int, val h: Int) {
        val x2 = x + w - 1
        val y2 = y + h - 1
    }

    @Test
    fun part01() {
        val numberRegex = """\d+""".toRegex()
        val regions = lines.flatMapIndexed { y, line ->
            numberRegex.findAll(line).map {
                Region2D(it.range.first, y, it.range.last - it.range.first + 1, 1) to it.value.toInt()
            }
        }
        val symbols = lines.flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, ch ->
                if (!ch.isDigit() && ch != '.') {
                    Point2D(x, y)
                } else {
                    null
                }
            }
        }.groupBy { it.y }
        val partNumbers = regions.filter { (range, _) ->
            for (i in (range.y - 1..range.y2 + 1)) {
                for (symbol in symbols.getOrDefault(i, emptyList())) {
                    if (symbol.x >= range.x - 1 && symbol.x <= range.x2 + 1) {
                        return@filter true
                    }
                }
            }

            false
        }

        println(partNumbers.map { it.second }.sum())
    }

    @Test
    fun part02() {
    }
}
