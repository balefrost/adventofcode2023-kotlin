package org.balefrost.aoc2023

import java.util.*

fun CharSequence.prefixesShortToLong() = sequence {
    for (i in 0..this@prefixesShortToLong.length) {
        yield(this@prefixesShortToLong.substring(0, i))
    }
}

fun CharSequence.prefixesLongToShort() = sequence {
    for (i in this@prefixesLongToShort.length.downTo(0)) {
        yield(this@prefixesLongToShort.substring(0, i))
    }
}

fun CharSequence.suffixesShortToLong() = sequence {
    for (i in this@suffixesShortToLong.length.downTo(0)) {
        yield(this@suffixesShortToLong.substring(i, this@suffixesShortToLong.length))
    }
}

fun CharSequence.suffixesLongToShort() = sequence {
    for (i in 0..this@suffixesLongToShort.length) {
        yield(this@suffixesLongToShort.substring(i, this@suffixesLongToShort.length))
    }
}

data class RunInfo<K, T>(val key: K, val values: List<T>)

fun <K, T> Sequence<T>.groupRunsBy(fn: (T) -> K): Sequence<RunInfo<K, T>> {
    return sequence {
        val iter = iterator()
        if (iter.hasNext()) {
            var v = iter.next()
            var currentKey = fn(v)
            var run = mutableListOf(v)
            while (iter.hasNext()) {
                v = iter.next()
                val k = fn(v)
                if (k == currentKey) {
                    run += v
                } else {
                    yield(RunInfo(currentKey, run))
                    currentKey = k
                    run = mutableListOf(v)
                }
            }
            if (run.isNotEmpty()) {
                yield(RunInfo(currentKey, run))
            }
        }
    }
}

data class RunLength<T>(val item: T, val length: Int)

fun <T> Sequence<T>.runLengths(): Sequence<RunLength<T>> {
    val s = this
    return sequence {
        val iter = s.iterator()
        if (iter.hasNext()) {
            var count = 1
            var item = iter.next()

            while (iter.hasNext()) {
                val newItem = iter.next()
                if (newItem == item) {
                    count += 1
                } else {
                    yield(RunLength(item, count))
                    item = newItem
                    count = 1
                }
            }
            yield(RunLength(item, count))
        }
    }
}

data class Point2D(val x: Int, val y: Int) {
    operator fun plus(dir: Dir2D): Point2D {
        return Point2D(x + dir.dx, y + dir.dy)
    }

    operator fun minus(dir: Dir2D): Point2D {
        return Point2D(x - dir.dx, y - dir.dy)
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}

data class Dir2D(val dx: Int, val dy: Int) {
    operator fun plus(pt: Point2D): Point2D {
        return pt + this
    }

    fun rotateLeft(): Dir2D {
        return Dir2D(dy, -dx)
    }

    fun rotateRight(): Dir2D {
        return Dir2D(-dy, dx)
    }

    operator fun unaryMinus(): Dir2D {
        return Dir2D(-dx, -dy)
    }

    operator fun times(factor: Int): Dir2D {
        return Dir2D(dx * factor, dy * factor)
    }

    val isHorizontal: Boolean get() = dy == 0

    val isVertical: Boolean get() = dx == 0

    override fun toString(): String {
        return "<$dx, $dy>"
    }
}

data class Region2D(val x: Int, val y: Int, val w: Int, val h: Int) {
    operator fun contains(pt: Point2D): Boolean {
        return pt.x >= x && pt.x < x + w && pt.y >= y && pt.y < y + h
    }
}

fun <K, V : Comparable<V>> dijkstra(
    initialKeysAndScores: Iterable<Pair<K, V>>,
    getAdjacent: (K) -> Iterable<K>,
    getTentativeScore: (V, K) -> V,
    isEndState: (K) -> Boolean
): Pair<V, List<K>>? {
    data class ResultEntry(val score: V, val previous: K?)

    val results = mutableMapOf<K, ResultEntry>()
    val toProcess = PriorityQueue<K>(compareBy { results.getValue(it).score })
    for ((key, score) in initialKeysAndScores) {
        results[key] = ResultEntry(score, null)
        toProcess += key
    }
    val completed = mutableSetOf<K>()

    while (toProcess.isNotEmpty()) {
        val key = toProcess.remove()
        if (isEndState(key)) {
            val path = generateSequence(key) { results.getValue(it).previous }.toList().asReversed()
            val finalScore = results.getValue(key).score
            return finalScore to path
        }
        val adjacent = getAdjacent(key)
        for (adj in adjacent) {
            if (adj in completed) {
                continue
            }
            val candidateScore = getTentativeScore(results.getValue(key).score, adj)
            val candidate = ResultEntry(candidateScore, key)
            var added = false
            results.compute(adj) { _, existing ->
                if (existing != null && existing.score <= candidateScore) {
                    existing
                } else {
                    added = true
                    candidate
                }
            }
            if (added) {
                toProcess.add(adj)
            }
        }
    }
    return null
}

fun IntRange.updateFirst(newFirst: Int): IntRange = IntRange(newFirst, this.last)
fun IntRange.updateLast(newLast: Int): IntRange = IntRange(this.first, newLast)

fun IntRange.shift(delta: Int): IntRange = IntRange(first + delta, last + delta)