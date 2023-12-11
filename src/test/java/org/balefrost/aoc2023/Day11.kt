package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import kotlin.math.abs

class Day11 {
    val sampleText = """
        ...#......
        .......#..
        #.........
        ..........
        ......#...
        .#........
        .........#
        ..........
        .......#..
        #...#.....
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day11.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Point2D(val x: Long, val y: Long) {
        fun manhattanDistance(dst: Point2D): Long {
            return abs(x - dst.x) + abs(y - dst.y)
        }
    }

    fun solve(expansion: Long): Long {
        val rowOffsets = mutableListOf<Long>()
        var cumulativeRowOffset = 0L
        lines.forEachIndexed { index, line ->
            if (line.all { it == '.'}) {
                cumulativeRowOffset += expansion
            }
            rowOffsets += cumulativeRowOffset
            cumulativeRowOffset++
        }

        val colOffsets = mutableListOf<Long>()
        var cumulativeColOffset = 0L
        (0 until lines.first().length).forEach { col ->
            if (lines.all { line -> line[col] == '.' }) {
                cumulativeColOffset += expansion
            }
            colOffsets += cumulativeColOffset
            cumulativeColOffset++
        }

        val galaxies = mutableListOf<Point2D>()
        lines.forEachIndexed { row, line ->
            line.forEachIndexed { col, ch ->
                if (ch == '#') {
                    galaxies += Point2D(col.toLong(), row.toLong())
                }
            }
        }

        var totalDistance = 0L
        for (src in galaxies.indices) {
            for (dst in src + 1 .. galaxies.lastIndex) {
                val srcGal = galaxies[src]
                val dstGal = galaxies[dst]
                val srcFinalPos = Point2D(colOffsets[srcGal.x.toInt()], rowOffsets[srcGal.y.toInt()])
                val dstFinalPos = Point2D(colOffsets[dstGal.x.toInt()], rowOffsets[dstGal.y.toInt()])
                totalDistance += srcFinalPos.manhattanDistance(dstFinalPos)
            }
        }
        return totalDistance
    }

    @Test
    fun part01() {
        val totalDistance = solve(1)

        println(totalDistance)

    }

    @Test
    fun part02() {
        val totalDistance = solve(999999)

        println(totalDistance)
    }

}