package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day23 {
    val sampleText = """
        #.#####################
        #.......#########...###
        #######.#########.#.###
        ###.....#.>.>.###.#.###
        ###v#####.#v#.###.#.###
        ###.>...#.#.#.....#...#
        ###v###.#.#.#########.#
        ###...#.#.#.......#...#
        #####.#.#.#######.#.###
        #.....#.#.#.......#...#
        #.#####.#.#.#########v#
        #.#...#...#...###...>.#
        #.#.#v#######v###.###v#
        #...#.>.#...>.>.#.###.#
        #####v#.#.###v#.#.###.#
        #.....#...#...#.#.#...#
        #.#########.###.#.#.###
        #...###...#...#...#.###
        ###.###.#.###v#####v###
        #...#...#.#.>.>.#.>.###
        #.###.###.#.###.#.#v###
        #.....###...###...#...#
        #####################.#
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day23.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val startingPos = Point2D(lines.first().indexOf('.'), 0)
    val endingPos = Point2D(lines.last().indexOf('.'), lines.lastIndex)
    val bounds = Region2D(0, 0, lines.first().length, lines.size)

    fun get(pos: Point2D): Char = if (pos in bounds) lines[pos.y][pos.x] else '#'

    fun exitsPt1(pos: Point2D): List<Point2D> {
        if (pos !in bounds) {
            return emptyList()
        }

        val adjacentDirs = listOf(
            Dir2D(1, 0) to '>',
            Dir2D(0, 1) to 'v',
            Dir2D(-1, 0) to '<',
            Dir2D(0, -1) to '^'
        )

        return adjacentDirs.mapNotNull { (dir, allowed) ->
            val adj = pos + dir
            val ch = get(adj)
            if (ch == '.' || ch == allowed) {
                adj
            } else {
                null
            }
        }
    }

    fun exitsPt2(pos: Point2D): List<Point2D> {
        if (pos !in bounds) {
            return emptyList()
        }

        return pos.adjacent.filter { adj ->
            val ch = get(adj)
            ch != '#'
        }
    }

    @Test
    fun part01() {
        val walks = sequence<Iterable<Point2D>> {
            suspend fun SequenceScope<Iterable<Point2D>>.helper(depth: Int, pos: Point2D, walk: LinkedHashSet<Point2D>) {
                var currentPos = pos
                val newWalk = LinkedHashSet<Point2D>(walk.size + 1)
                newWalk.addAll(walk)
                while (true) {
                    newWalk += currentPos

                    if (currentPos == endingPos) {
                        yield(newWalk)
                        return
                    }

                    val exits = exitsPt1(currentPos).filter { it !in newWalk }
                    if (exits.size == 0) {
                        newWalk.toString()
                        currentPos.toString()
                        return
                    } else if (exits.size == 1) {
                        currentPos = exits.single()
                    }else  {
                        for (exit in exits) {
                            helper(depth + 1, exit, newWalk)
                        }
                        return
                    }
                }
            }

            helper(0, startingPos + Dir2D(0, 1), linkedSetOf(startingPos))
        }

        println(walks.map { it.count() - 1}.max())

    }

    @Test
    fun part02a() {
    }

}