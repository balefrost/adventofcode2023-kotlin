package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day10 {
    val sampleText = """
        FF7FSF7F7F7F7F7F---7
        L|LJ||||||||||||F--J
        FL-7LJLJ||||||LJL-77
        F--JF--7||LJLJ7F7FJ-
        L---JF-JLJ.||-FJLJJ7
        |F|F-JF---7F7-L7L|7|
        |FFJF7L7F-JF7|JL---7
        7-L-JL7||F7|L7F-7F7|
        L.L7LFJ|||||FJL7||LJ
        L7JLJL-JLJLJL--JLJ.L
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day10.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Point2D(val x: Int, val y: Int) {
        operator fun plus(dir: Dir2D): Point2D {
            return Point2D(x + dir.x, y + dir.y)
        }

        operator fun minus(p: Point2D): Dir2D {
            return Dir2D(x - p.x, y - p.y)
        }
    }

    data class Dir2D(val x: Int, val y: Int) {
        operator fun plus(p: Point2D): Point2D {
            return p + this
        }
    }

    val mappings = mapOf(
        '|' to setOf(Dir2D(0, 1), Dir2D(0, -1)),
        '-' to setOf(Dir2D(1, 0), Dir2D(-1, 0)),
        'L' to setOf(Dir2D(1, 0), Dir2D(0, -1)),
        'J' to setOf(Dir2D(-1, 0), Dir2D(0, -1)),
        '7' to setOf(Dir2D(-1, 0), Dir2D(0, 1)),
        'F' to setOf(Dir2D(1, 0), Dir2D(0, 1)),
        '.' to emptySet()
    )

    val invMappings = mapOf(
        setOf(Dir2D(0, 1), Dir2D(0, -1)) to '|',
        setOf(Dir2D(1, 0), Dir2D(-1, 0)) to '-',
        setOf(Dir2D(1, 0), Dir2D(0, -1)) to 'L',
        setOf(Dir2D(-1, 0), Dir2D(0, -1)) to 'J',
        setOf(Dir2D(-1, 0), Dir2D(0, 1)) to '7',
        setOf(Dir2D(1, 0), Dir2D(0, 1)) to 'F'
    )

    fun explore(start: Point2D): List<Point2D> {
        dirs@
        for (dir in listOf(Dir2D(0, 1), Dir2D(0, -1), Dir2D(1, 0), Dir2D(-1, 0))) {
            var curr = start + dir
            var prev = start
            val visited = mutableListOf<Point2D>()
            while (curr != start) {
                val adjacent = mappings.getValue(lines[curr.y][curr.x]).map { it + curr }
                val (towardPrev, towardNext) = adjacent.partition { it == prev }
                if (towardPrev.isEmpty()) {
                    continue@dirs
                }
                visited += curr
                prev = curr
                curr = towardNext.first()
            }
            return visited
        }
        return emptyList()
    }

    fun findStart(): Point2D {
        for (y in lines.indices) {
            val line = lines[y]
            for (x in line.indices) {
                val ch = line[x]
                if (ch == 'S') {
                    return Point2D(x, y)
                }
            }
        }
        throw Exception("Start not found")
    }

    @Test
    fun part01() {
        val start = findStart()
        println(explore(start).size / 2 + 1)
    }

    enum class InsideOrOutside {
        Outside,
        OutsideRidingLine,
        Inside,
        InsideRidingLine;

        fun startRidingLine(): InsideOrOutside {
            return when (this) {
                Outside -> OutsideRidingLine
                Inside -> InsideRidingLine
                else -> throw Exception("Can't start riding line from $this")
            }
        }

        fun stopRidingLine(howStarted: Char, howStopped: Char): InsideOrOutside {
            val flip = when(howStarted to howStopped) {
                ('L' to 'J') -> false
                ('L' to '7') -> true
                ('F' to 'J') -> true
                ('F' to '7') -> false
                else -> throw Exception("Can't compute flip")
            }
            return when (this) {
                OutsideRidingLine -> if (flip) Inside else Outside
                InsideRidingLine -> if (flip) Outside else Inside
                else -> throw Exception("Can't stop riding line from $this")
            }
        }
    }

    @Test
    fun part02() {
        val start = findStart()
        val path = explore(start)
        val startCh = invMappings.getValue(listOf(path.first(), path.last()).map { it - start }.toSet())

        fun getCh(p: Point2D): Char = if (p == start) startCh else lines[p.y][p.x]

        val loopLocs = listOf(start) + path
        val loopLocsByLine = loopLocs.groupBy { it.y }

        var totalInside = 0
        for (y in lines.indices) {
            val loopParts = loopLocsByLine.getOrDefault(y, emptyList())
            val sortedLoopParts = loopParts.sortedBy { it.x }
            var totalInsideThisRow = 0
            var lastX = -1

            var insideOrOutside = InsideOrOutside.Outside
            // When insideOrOutside is one of the "RidingLine" states, the character that started the sequence
            var howStarted = '!'

            for (part in sortedLoopParts) {
                if (insideOrOutside == InsideOrOutside.Inside) {
                    totalInsideThisRow += part.x - lastX - 1
                }
                val ch = getCh(part)
                when (ch) {
                    '-' -> continue
                    '|' -> {
                        insideOrOutside = when (insideOrOutside) {
                            InsideOrOutside.Outside -> InsideOrOutside.Inside
                            InsideOrOutside.Inside -> InsideOrOutside.Outside
                            else -> throw Exception("Unexpected |")
                        }
                    }
                    'L' -> {
                        insideOrOutside = insideOrOutside.startRidingLine()
                        howStarted = ch
                    }
                    'F' -> {
                        insideOrOutside = insideOrOutside.startRidingLine()
                        howStarted = ch
                    }
                    'J' -> {
                        insideOrOutside = insideOrOutside.stopRidingLine(howStarted, ch)
                        howStarted = '!'
                    }
                    '7' -> {
                        insideOrOutside = insideOrOutside.stopRidingLine(howStarted, ch)
                        howStarted = '!'
                    }
                }
                lastX = part.x
            }
            totalInside += totalInsideThisRow
        }

        println(totalInside)
    }

}