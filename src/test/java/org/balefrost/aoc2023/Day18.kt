package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import java.util.PriorityQueue

class Day18 {
    val sampleText = """
        R 6 (#70c710)
        D 5 (#0dc571)
        L 2 (#5713f0)
        D 2 (#d2c081)
        R 2 (#59c680)
        D 2 (#411b91)
        L 5 (#8ceee2)
        U 2 (#caa173)
        L 1 (#1b58a2)
        U 2 (#caa171)
        R 2 (#7807d2)
        U 3 (#a77fa3)
        L 2 (#015232)
        U 2 (#7a21e3)
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day18.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val lineRegex = """(\w) (\d+) \(#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})\)""".toRegex()

    data class RGB(val r: UByte, val g: UByte, val b: UByte)

    data class Line(val dir: Char, val dist: Int, val color: RGB)

    val input = lines.map { line ->
        val values = lineRegex.matchEntire(line)!!.groupValues
        val dir = values[1]
        val dist = values[2]
        val r = values[3]
        val g = values[4]
        val b = values[5]
        val color = RGB(r.toUByte(16), g.toUByte(16), b.toUByte(16))
        Line(dir[0], dist.toInt(), color)
    }

    val charToDir = mapOf(
        'R' to Dir2D(1, 0),
        'D' to Dir2D(0, 1),
        'L' to Dir2D(-1, 0),
        'U' to Dir2D(0, -1)
    )

    // 126292 not right
    @Test
    fun part01() {
        val dugSquares = mutableMapOf<Point2D, Dir2D>()

        var currentPos = Point2D(0, 0)
        for (line in input) {
            val dir = charToDir.getValue(line.dir)
            if (line.dir in setOf('U', 'D')) {
                dugSquares[currentPos] = dir
            }
            repeat(line.dist) {
                currentPos += dir
                dugSquares[currentPos] = dir
            }
        }

//        val minX = dugSquares.map { it.x }.min()
//        val maxX = dugSquares.map { it.x }.max()
//        val minY = dugSquares.map { it.y }.min()
//        val maxY = dugSquares.map { it.y }.max()
//
//        for (y in minY..maxY) {
//            for (x in minX .. maxX) {
//                print(if (Point2D(x, y) in dugSquares) '#' else '.')
//            }
//            println()
//        }

        val dugSquaresByY = dugSquares.entries.groupBy { it.key.y }.toSortedMap()
        val result = dugSquaresByY.map { (y, xs) ->
            val sortedByXs = xs.sortedBy { it.key.x }
            var totalSpan = 0
            var i = 0
            while (i < sortedByXs.size) {
                val entryDir = sortedByXs[i].value
                val entryX = sortedByXs[i].key.x
                ++i
                while (sortedByXs[i].value != -entryDir) {
                    ++i
                }
                while (i < sortedByXs.size && sortedByXs[i].value != entryDir) {
                    ++i
                }
                val exitX = sortedByXs[i - 1].key.x
                totalSpan += exitX - entryX + 1
            }
            totalSpan
        }.sum()

        println(result)
    }

    @Test
    fun part02() {
    }

}