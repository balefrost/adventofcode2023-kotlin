package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min

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

    val lineRegex = """(\w) (\d+) \(#([0-9a-f]{5})([0-9a-f])\)""".toRegex()

    data class Instruction(val dir: Char, val dist: Int)
    data class Line(val p1: Point2D, val p2: Point2D, val length: Int, val dir: Dir2D) {
        val xRange: IntRange get() = IntRange(min(p1.x, p2.x), max(p1.x, p2.x))
        val yRange: IntRange get() = IntRange(min(p1.y, p2.y), max(p1.y, p2.y))

        val isHorizontal: Boolean get() = dir.isHorizontal
        val isVertical: Boolean get() = dir.isVertical

        override fun toString(): String {
            return "$p1 -> $p2"
        }
    }

    val part1Input = lines.map { line ->
        val values = lineRegex.matchEntire(line)!!.groupValues
        val dir = values[1][0]
        val dist = values[2].toInt()
        Instruction(dir, dist)
    }

    val part2Input = lines.map { line ->
        val values = lineRegex.matchEntire(line)!!.groupValues
        val dist = values[3].toInt(16)
        val dir = when (values[4][0]) {
            '0' -> 'R'
            '1' -> 'D'
            '2' -> 'L'
            '3' -> 'U'
            else -> error("Invalid direction")
        }
        Instruction(dir, dist)
    }

    val charToDir = mapOf(
        'R' to Dir2D(1, 0),
        'D' to Dir2D(0, 1),
        'L' to Dir2D(-1, 0),
        'U' to Dir2D(0, -1)
    )

    interface IntervalLookup<K : Comparable<K>, T> {
        fun findRelevant(i: K): Set<T>

        val items: Iterable<Pair<OpenEndRange<K>, Set<T>>>
    }

    fun <K: Comparable<K>, T> makeIntervalLookup(items: Iterable<T>, makeRange: (T) -> OpenEndRange<K>): IntervalLookup<K, T> {

        class Lookup(val intervals: List<Pair<OpenEndRange<K>, Set<T>>>) : IntervalLookup<K, T> {
            override fun findRelevant(k: K): Set<T> {
                val idx = intervals.binarySearch { (range, _) ->
                    when {
                        k < range.start -> -1
                        k >= range.endExclusive -> 1
                        else -> 0
                    }
                }
                return when {
                    idx > 0 -> intervals[idx].second
                    else -> emptySet()
                }
            }

            override val items: Iterable<Pair<OpenEndRange<K>, Set<T>>> get() = intervals
        }

        data class Event(val i: K, val enter: Boolean, val item: T)

        val sortedEvents = items.flatMap { item ->
            val range = makeRange(item)
            listOf(
                Event(range.start, true, item),
                Event(range.endExclusive, false, item)
            )
        }.groupBy { it.i }.toSortedMap()

        val resultLookup = mutableListOf<Pair<OpenEndRange<K>, Set<T>>>()
        val activeSet = mutableMapOf<T, Int>()
        var previousK: K? = null
        for ((k, events) in sortedEvents) {
            if (previousK != null && activeSet.isNotEmpty()) {
                resultLookup += ((previousK.rangeUntil(k)) to activeSet.keys.toSet())
            }
            for (event in events) {
                activeSet.merge(event.item, if (event.enter) 1 else -1) { existing, update ->
                    val newCount = existing + update
                    when {
                        newCount < 0 -> error("negative count")
                        newCount == 0 -> null
                        else -> newCount
                    }
                }
            }
            previousK = k
        }
        return Lookup(resultLookup)
    }

    fun solve(instructions: List<Instruction>): Long {
        var currentPos = Point2D(0, 0)
        val lines = mutableListOf<Line>()
        for (instruction in instructions) {
            val dir = charToDir.getValue(instruction.dir)
            val newPos = currentPos + dir * instruction.dist
            lines += Line(currentPos, newPos, instruction.dist, dir)
            currentPos = newPos
        }

        val yIntervals = makeIntervalLookup(lines) { line -> line.yRange}

        var totalArea = 0L
        for ((yRange, linesInRange) in yIntervals.items) {
            val h = yRange.endExclusive - yRange.start
            fun accumWidth(w: Int) {
                totalArea += (h.toLong() * w)
            }
            val sortedLines = linesInRange.sortedWith(compareBy<Line> { it.xRange.first }.thenBy { it.xRange.last })
            var i = 0
            fun currentLine() = sortedLines[i]
            fun hasLine() = i < sortedLines.size
            while (i < sortedLines.size) {
                check (currentLine().isVertical)
                val enterDir = currentLine().dir
                val enterX = currentLine().xRange.first
                ++i
                if (currentLine().isHorizontal) {
                    ++i
                    val line = currentLine()
                    ++i
                    check(line.isVertical)
                    if (line.dir == -enterDir) {
                        val exitX = line.xRange.last
                        accumWidth(exitX - enterX + 1)
                        continue
                    }
                }

                var exitX: Int
                while (true) {
                    check(currentLine().isVertical)
                    val exitDir = currentLine().dir
                    exitX = currentLine().xRange.last
                    ++i
                    if (hasLine() && currentLine().isHorizontal) {
                        ++i
                        val line = currentLine()
                        ++i
                        check(line.isVertical)
                        if (line.dir == exitDir) {
                            exitX = line.xRange.last
                            break
                        }
                    } else {
                        break
                    }
                }

                accumWidth(exitX - enterX + 1)
            }
        }

        return totalArea
    }

    @Test
    fun part01() {
        println(solve(part1Input))
    }

    @Test
    fun part02() {
        println(solve(part2Input))
    }

}