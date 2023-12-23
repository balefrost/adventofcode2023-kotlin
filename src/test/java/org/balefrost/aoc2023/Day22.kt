package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day22 {
    val sampleText = """
        1,0,1~1,2,1
        0,0,2~2,0,2
        0,2,3~2,2,3
        0,0,4~0,2,4
        2,0,5~2,2,5
        0,1,6~2,1,6
        1,1,8~1,1,9
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day22.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val lineRegex = """(\d+),(\d+),(\d+)~(\d+),(\d+),(\d+)""".toRegex()

    data class Brick(val x: IntRange, val y: IntRange, val z: IntRange) {
        fun overlapsInXY(other: Brick): Boolean {
            if (x.last < other.x.first) {
                return false
            }
            if (x.first > other.x.last) {
                return false
            }
            if (y.last < other.y.first) {
                return false
            }
            if (y.first > other.y.last) {
                return false
            }
            return true
        }

        fun isAbove(other: Brick): Boolean {
            if (!overlapsInXY(other)) {
                return false
            }
            return z.first > other.z.last
        }

        fun isRestingOn(other: Brick): Boolean {
            if (!overlapsInXY(other)) {
                return false
            }
            return z.first == other.z.last + 1
        }
    }

    val initial = lines.map {
        val m = lineRegex.matchEntire(it)!!
        val x1 = m.groupValues[1].toInt()
        val y1 = m.groupValues[2].toInt()
        val z1 = m.groupValues[3].toInt()
        val x2 = m.groupValues[4].toInt()
        val y2 = m.groupValues[5].toInt()
        val z2 = m.groupValues[6].toInt()
        Brick(IntRange(x1, x2), IntRange(y1, y2), IntRange(z1, z2))
    }

    fun findBrickDependency(initial: List<Brick>): Map<Int, Set<Int>> {
        val sortedBricks = initial.withIndex().sortedBy { it.value.z.first }
        val result = mutableMapOf<Int, Set<Int>>()
        for (i in sortedBricks.indices) {
            val a = sortedBricks[i]
            val dependencies = mutableSetOf<Int>()
            for (j in 0..<i) {
                val b = sortedBricks[j]
                if (a.value.isAbove(b.value)) {
                    dependencies += b.index
                }
            }
            result[a.index] = dependencies
        }
        return result
    }

    fun <T> invertDependencies(deps: Map<T, Iterable<T>>): Map<T, Set<T>> {
        val result = mutableMapOf<T, MutableSet<T>>()
        for ((key, myDeps) in deps) {
            for (dep in myDeps) {
                result.getOrPut(dep) { mutableSetOf() } += key
            }
        }
        return result
    }

//    fun dropBricks(bricks: List<Brick>): Boolean {
//    }

    fun <T> linearizeDeps(deps: Map<T, Iterable<T>>): Sequence<T> {
        return sequence {
            val emitted = mutableSetOf<T>()
            suspend fun SequenceScope<T>.helper(item: T) {
                for (dep in deps[item] ?: emptySet()) {
                    if (dep !in emitted) {
                        helper(dep)
                    }
                }
                emitted += item
                yield(item)
            }
            for (item in deps.keys) {
                helper(item)
            }
        }
    }

    fun dropBricks(bricks: List<Brick>, deps: Map<Int, Iterable<Int>>): List<Brick> {
        val b = bricks.toMutableList()
        for (item in linearizeDeps(deps)) {
            val myDeps = deps.getValue(item)
            val maxZOfDeps = myDeps.map { b[it].z.last }.maxOrNull() ?: 0
            val targetZ = maxZOfDeps + 1
            val toAdjust = b[item].z.first - targetZ
            val current = b[item]
            b[item] = current.copy(z = current.z.shift(-toAdjust))
        }
        return b
    }

    @Test
    fun part01() {
        val deps = findBrickDependency(initial)
        val dropped = dropBricks(initial, deps)

        val restingUpon = deps.entries.associate { (item, deps) ->
            val brick = dropped[item]
            item to deps.filter { brick.isRestingOn(dropped[it]) }
        }

        val cannotBeDisintegrated = restingUpon.flatMap { (_, deps) ->
            when {
                deps.size < 2 -> deps
                else -> emptyList()
            }
        }.toSet()

        val canBeDisintegrated = restingUpon.keys - cannotBeDisintegrated

        println(canBeDisintegrated.size)
    }

    @Test
    fun part02a() {
        val deps = findBrickDependency(initial)
        val dropped = dropBricks(initial, deps)

        val restingUpon = deps.entries.associate { (item, deps) ->
            val brick = dropped[item]
            item to deps.filter { brick.isRestingOn(dropped[it]) }
        }

        val result = initial.indices.map { toDisintegrate ->
            val fallen = mutableSetOf(toDisintegrate)
            for (item in linearizeDeps(restingUpon)) {
                val depsOfItem = restingUpon[item] ?: emptySet()
                if (depsOfItem.isEmpty()) {
                    continue
                }
                if ((depsOfItem - fallen).isEmpty()) {
                    fallen += item
                }
            }
//            println("$toDisintegrate -> ${fallen.size - 1}")
            fallen.size - 1
        }.sum()

        println(result)
    }

}