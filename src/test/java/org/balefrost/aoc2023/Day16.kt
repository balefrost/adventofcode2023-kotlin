package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day16 {
    val sampleText = """
        .|...\....
        |.-.\.....
        .....|-...
        ........|.
        ..........
        .........\
        ..../.\\..
        .-.-/..|..
        .|....-|.\
        ..//.|....
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day16.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    data class Beam(val pos: Point2D, val dir: Dir2D)

    fun get(pos: Point2D): Char {
        if (pos.y < 0 || pos.y > lines.lastIndex) {
            return 'X'
        }
        val line = lines[pos.y]
        if (pos.x < 0 || pos.x > line.lastIndex) {
            return 'X'
        }
        return line[pos.x]
    }

    fun computeBounce(dir: Dir2D, mirror: Char): Dir2D {
        return when (mirror) {
            '/' -> when {
                dir.dy == 0 -> dir.rotateLeft()
                dir.dx == 0 -> dir.rotateRight()
                else -> error("Invalid dir")
            }
            '\\' -> when {
                dir.dy == 0 -> dir.rotateRight()
                dir.dx == 0 -> dir.rotateLeft()
                else -> error("Invalid dir")
            }
            else -> error("Invalid mirror")
        }
    }

    fun computeSplits(dir: Dir2D, splitter: Char): List<Dir2D> {
        return when (splitter) {
            '|' -> when {
                dir.dx == 0 -> listOf(dir)
                dir.dy == 0 -> listOf(dir.rotateLeft(), dir.rotateRight())
                else -> error("Invalid dir")
            }
            '-' -> when {
                dir.dx == 0 -> listOf(dir.rotateLeft(), dir.rotateRight())
                dir.dy == 0 -> listOf(dir)
                else -> error("Invalid dir")
            }
            else -> error("Invalid splitter")
        }
    }

    @Test
    fun part01() {
        println(computeEnergizedPoints(Beam(Point2D(-1, 0), Dir2D(1, 0))))
    }

    private fun computeEnergizedPoints(initialBeam: Beam): Int {
        val energizedPoints = mutableSetOf<Point2D>()
        val knownBeams = mutableSetOf<Beam>()
        val beams = ArrayDeque<Beam>()
        beams.addLast(initialBeam)

        while (beams.isNotEmpty()) {
            val beam = beams.removeFirst()
            if (knownBeams.add(beam)) {
                var currentPos = beam.pos
                while (true) {
                    currentPos += beam.dir
                    val ch = get(currentPos)
                    if (ch == 'X') {
                        break
                    }
                    energizedPoints += currentPos
                    when (ch) {
                        '.' -> continue
                        '/', '\\' -> {
                            val newDir = computeBounce(beam.dir, ch)
                            beams.addLast(Beam(currentPos, newDir))
                            break
                        }

                        '|', '-' -> {
                            for (newDir in computeSplits(beam.dir, ch)) {
                                beams.addLast(Beam(currentPos, newDir))
                            }
                            break
                        }
                    }
                }
            }
        }
        return energizedPoints.size
    }

    @Test
    fun part02() {
        val width = lines.first().length
        val height = lines.size

        val beamsToTest = (0 until width).flatMap { x ->
            listOf(
                Beam(Point2D(x, -1), Dir2D(0, 1)),
                Beam(Point2D(x, height), Dir2D(0, -1))
            )
        } + (0 until height).flatMap { y ->
            listOf(
                Beam(Point2D(-1, y), Dir2D(1, 0)),
                Beam(Point2D(width, y), Dir2D(-1, 0))
            )
        }

        println(beamsToTest.maxOf { computeEnergizedPoints(it) })
    }

}