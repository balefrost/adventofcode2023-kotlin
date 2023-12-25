package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day25 {
    val sampleText = """
        jqt: rhn xhk nvd
        rsh: frs pzl lsr
        xhk: hfx
        cmg: qnr nvd lhk bvb
        rhn: xhk bvb hfx
        bvb: xhk hfx
        pzl: lsr hfx nvd
        qnr: nvd
        ntq: jqt hfx bvb xhk
        nvd: lhk
        lsr: lhk
        rzs: qnr cmg lsr rsh
        frs: qnr lhk lsr
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day25.txt")!!.readText()

//        val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    fun canonicalEdge(edge: Pair<String, String>): Pair<String, String> {
        val (a, b) = edge
        return if (a <= b) {
            edge
        } else {
            b to a
        }
    }

    val edges = lines.flatMap { line ->
        val (a, bs) = line.split(": ".toRegex())
        bs.split(" ").map { b ->
            canonicalEdge(a to b)
        }
    }

    val adjacency = edges.flatMap { (a, b) ->
        listOf(a to b, b to a)
    }.groupBy({ it.first }, { it.second })

    fun findDisjointPaths(src: String, dst: String, usedEdges: Iterable<Pair<String, String>>): Sequence<Pair<Int, List<String>>> {
        return sequence {
            val missingEdges = mutableMapOf<String, MutableSet<String>>()
            for ((a, b) in usedEdges) {
                missingEdges.getOrPut(a, ::LinkedHashSet) += b
                missingEdges.getOrPut(b, ::LinkedHashSet) += a
            }
            fun getAdjacent(it: String): Iterable<String> {
                return adjacency.getValue(it) - (missingEdges as Map<String, Set<String>>).getOrDefault(it, emptySet())
            }
            while (true) {
                val result = dijkstra(listOf(src to 0), ::getAdjacent, { score, _ -> score + 1 }, { it == dst })
                if (result == null) {
                    break
                }
                yield(result)
                for ((a, b) in result.second.zipWithNext()) {
                    missingEdges.getOrPut(a, ::LinkedHashSet) += b
                    missingEdges.getOrPut(b, ::LinkedHashSet) += a
                }
            }
        }
    }

    @Test
    fun part01() {
        val vertices = adjacency.keys.toList()
        val src = vertices[0]
        val dsts = vertices.drop(1)
        val itemsInOtherComponent = dsts.count { dst ->
            val paths = findDisjointPaths(src, dst, emptyList()).take(4).toList()
            paths.size == 3
        }
        val itemsInThisComponent = vertices.size - itemsInOtherComponent
        println(itemsInThisComponent * itemsInOtherComponent)
    }

    @Test
    fun part02() {
    }

}