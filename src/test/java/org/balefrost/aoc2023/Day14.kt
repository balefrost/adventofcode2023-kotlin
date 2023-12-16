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

    enum class Direction(val dx: Int, val dy: Int) {
        North(0, -1),
        West(-1, 0),
        South(0, 1),
        East(1, 0);

        val dir: Dir2D get() = Dir2D(dx, dy)
    }

    fun iterationOrder(w: Int, h: Int, dir: Direction): Sequence<Point2D> {
        return when(dir) {
            Direction.North -> sequence {
                for (r in 0 until h) {
                    for (c in 0 until w) {
                        yield(Point2D(c, r))
                    }
                }
            }
            Direction.West -> sequence {
                for (c in 0 until w) {
                    for (r in 0 until h) {
                        yield(Point2D(c, r))
                    }
                }
            }
            Direction.South -> sequence {
                for (r in h - 1 downTo 0) {
                    for (c in 0 until w) {
                        yield(Point2D(c, r))
                    }
                }
            }
            Direction.East -> sequence {
                for (c in w - 1 downTo 0) {
                    for (r in 0 until h) {
                        yield(Point2D(c, r))
                    }
                }
            }
        }
    }

    fun moveRocks(rocks: List<List<Char>>, dir: Direction): List<List<Char>> {
        val result = rocks.mapTo(mutableListOf()) { it.toMutableList() }
        val rgn = Region2D(0, 0, rocks.first().size, rocks.size)
        iterationOrder(rocks.first().size, rocks.size, dir).forEach { (c, r) ->
            if (result[r][c] == 'O') {
                var dest = Point2D(c, r) + dir.dir
                while (dest in rgn) {
                    if (result[dest.y][dest.x] != '.') {
                        break
                    }
                    dest = dest + dir.dir
                }
                dest = dest - dir.dir
                result[r][c] = '.'
                result[dest.y][dest.x] = 'O'
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

    fun printRocks(rocks: List<List<Char>>) {
        for (row in rocks) {
            for (c in row) {
                print(c)
            }
            println()
        }
    }

    @Test
    fun part01() {
        val moved = moveRocks(lines.map { it.toList()}, Direction.North)
        println(computeLoad(moved))
    }

    fun tumble(rocks: List<List<Char>>): List<List<Char>> {
        var r = rocks
        r = moveRocks(r, Direction.North)
        r = moveRocks(r, Direction.West)
        r = moveRocks(r, Direction.South)
        r = moveRocks(r, Direction.East)
        return r
    }

    fun tumbleTimes(rocks: List<List<Char>>, count: Long): List<List<Char>> {
        val tumbles = generateSequence(rocks, ::tumble)
        val configurationToIndex = mutableMapOf<List<List<Char>>, Int>()
        val configurations = mutableListOf<List<List<Char>>>()
        for ((idx, tumble) in tumbles.withIndex()) {
            configurations += tumble
            val existingIndex = configurationToIndex.putIfAbsent(tumble, idx)
            if (existingIndex != null) {
                val prefixLength = existingIndex
                val cycleLength = idx - existingIndex


                val finalIdx = (count - prefixLength).mod(cycleLength) + prefixLength
                return configurations[finalIdx]
            }
        }
        error("Impossible")
    }

    @Test
    fun part02() {
        val initial = lines.map { it.toList() }
        val final = tumbleTimes(initial, 1000000000)
        println(computeLoad(final))
    }

}