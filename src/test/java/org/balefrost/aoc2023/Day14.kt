package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day14 {
    val sampleText = """
        O....#....
        O.OO#....#
        .....##...
        OO.#O....O
        .O.....O#.
        O.#..O.#.#
        ..O..#O..O
        .......O..
        #....###..
        #OO..#....
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day14.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    fun moveRocks(rocks: List<List<Char>>): List<List<Char>> {
        val result = rocks.mapTo(mutableListOf<MutableList<Char>>()) { it.toMutableList() }
        for (r in 1..result.lastIndex) {
            val row = result[r]
            for (c in row.indices) {
                if (row[c] == 'O') {
                    var r2 = r - 1
                    while (r2 >= 0) {
                        if (result[r2][c] != '.') {
                            break
                        }
                        --r2
                    }
                    result[r][c] = '.'
                    result[r2 + 1][c] = 'O'
                }
            }
        }
        return result
    }

    fun computeLoad(rocks: List<List<Char>>): Int {
        var totalLoad = 0
        for (r in rocks.indices) {
            val row = rocks[r]
            for (c in row.indices) {
                if (row[c] == 'O') {
                    totalLoad += (rocks.size - r)
                }
            }
        }
        return totalLoad
    }

    @Test
    fun part01() {
        lines.forEach(::println)
        println()
        val moved = moveRocks(lines.map { it.toList()})
        moved.forEach {
            println(it.joinToString(""))
        }
        println(computeLoad(moved))
    }

    @Test
    fun part02() {
    }

}