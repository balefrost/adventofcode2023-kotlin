package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import kotlin.math.max

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

//        val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val startingPos = Point2D(lines.first().indexOf('.'), 0)
    val endingPos = Point2D(lines.last().indexOf('.'), lines.lastIndex)
    val bounds = Region2D(0, 0, lines.first().length, lines.size)

    fun get(pos: Point2D): Char = if (pos in bounds) lines[pos.y][pos.x] else '#'

    val allowedSlides = mapOf(
        Dir2D(1, 0) to '>',
        Dir2D(0, 1) to 'v',
        Dir2D(-1, 0) to '<',
        Dir2D(0, -1) to '^'
    )

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
            suspend fun SequenceScope<Iterable<Point2D>>.helper(
                depth: Int,
                pos: Point2D,
                walk: LinkedHashSet<Point2D>
            ) {
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
                    } else {
                        for (exit in exits) {
                            helper(depth + 1, exit, newWalk)
                        }
                        return
                    }
                }
            }

            helper(0, startingPos + Dir2D(0, 1), linkedSetOf(startingPos))
        }

        println(walks.map { it.count() - 1 }.max())

    }

    @Test
    fun part02() {
        data class Edge(val from: Point2D, val to: Point2D, val distance: Int)

        val edges: Map<Point2D, List<Edge>>

        run {
            val es = mutableSetOf<Edge>()

            data class Segment(val startingPos: Point2D, val nextPos: Point2D)

            val visited = mutableSetOf(startingPos)
            val toWalk = ArrayDeque<Segment>()
            toWalk.addLast(Segment(startingPos, startingPos + Dir2D(0, 1)))

            while (toWalk.isNotEmpty()) {
                val segment = toWalk.removeFirst()
                var currentPos = segment.nextPos
                val walk = LinkedHashSet<Point2D>(listOf(segment.startingPos))

                fun aggregateResult() {
                    es += Edge(segment.startingPos, currentPos, walk.size - 1)
                    es += Edge(currentPos, segment.startingPos, walk.size - 1)
                }

                while (true) {
                    walk += currentPos
                    visited += currentPos
                    if (currentPos == endingPos) {
                        aggregateResult()
                        break
                    }

                    val exits = exitsPt2(currentPos)
                    when {
                        exits.size == 2 -> {
                            currentPos = exits.single { it !in walk }
                        }

                        else -> {
                            aggregateResult()
                            for (exit in exits) {
                                if (exit in visited) {
                                    continue
                                }
                                toWalk.addLast(Segment(currentPos, exit))
                            }
                            break
                        }
                    }
                }
            }
            edges = es.groupBy { it.from }
        }

        val vertices = edges.keys
        println(vertices.size)

        if(false) {
            println("strict graph {")
            val pointToIndex = edges.keys.withIndex().associate { it.value to it.index }
            edges.keys.forEach { vertex ->
                val srcIndex = pointToIndex.getValue(vertex)
                println("  N$srcIndex [label=\"$vertex\"]")
            }
            println()
            edges.forEach { (_, edges) ->
                edges.forEach { edge ->
                    val srcIndex = pointToIndex.getValue(edge.from)
                    val dstIndex = pointToIndex.getValue(edge.to)
                    println("  N$srcIndex -- N$dstIndex [label=\"${edge.distance}\"]")
                }
            }
            println("}")
        }

        var bestWalkDistance = 0L
        val visitedVertices = mutableSetOf<Point2D>()
        fun helper(vertex: Point2D, distanceSoFar: Long) {
            if (vertex == endingPos) {
                bestWalkDistance = max(bestWalkDistance, distanceSoFar)
            } else {
                val egressEdges = edges.getValue(vertex).filter { it.to !in visitedVertices }
                for (edge in egressEdges) {
                    visitedVertices += vertex
                    helper(edge.to, distanceSoFar + edge.distance)
                    visitedVertices -= vertex
                }
            }
        }
        helper(startingPos, 0L)

        println(bestWalkDistance)
    }

}