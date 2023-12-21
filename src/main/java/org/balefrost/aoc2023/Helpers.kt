package org.balefrost.aoc2023

import java.util.*
import kotlin.math.min

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

fun stronglyConnectedComponents(numItems: Int, adjacency: Map<Int, Iterable<Int>>): Sequence<Set<Int>> {
    return sequence {
        var nextIndex = 0
        val itemIndex = IntArray(numItems) { -1 }
        val itemLowLink = IntArray(numItems) { -1 }
        val itemOnStack = BooleanArray(numItems) { false }
        val potentialRoots = Stack<Int>()

        suspend fun SequenceScope<Set<Int>>.strongConnect(v: Int) {
            itemIndex[v] = nextIndex
            itemLowLink[v] = nextIndex
            ++nextIndex
            potentialRoots.push(v)
            itemOnStack[v] = true

            for (adj in adjacency.getValue(v)) {
                if (itemIndex[adj] == -1) {
                    strongConnect(adj)
                    itemLowLink[v] = min(itemLowLink[v], itemLowLink[adj])
                } else if (itemOnStack[adj]) {
                    itemLowLink[v] = min(itemLowLink[v], itemIndex[adj])
                }
            }

            if (itemLowLink[v] == itemIndex[v]) {
                val s = mutableSetOf<Int>()
                while (true) {
                    val item = potentialRoots.pop()
                    itemOnStack[item] = false
                    s += item
                    if (item == v) {
                        break
                    }
                }
                yield(s)
            }
        }

        repeat(numItems) { v ->
            if (itemIndex[v] == -1) {
                strongConnect(v)
            }
        }
    }
}

fun <T> stronglyConnectedComponents(items: Iterable<T>, getAdjacent: (T) -> Iterable<T>): Sequence<Set<T>> {
    val itemToInt = mutableMapOf<T, Int>()
    val intToItem = mutableListOf<T>()

    for (item in items) {
        itemToInt[item] = intToItem.size
        intToItem += item
    }

    val adjacencyMap = mutableMapOf<Int, List<Int>>()
    for (item in items) {
        val k = itemToInt.getValue(item)
        val vs = getAdjacent(item).map(itemToInt::getValue)
        adjacencyMap[k] = vs
    }

    return stronglyConnectedComponents(itemToInt.size, adjacencyMap).map { ii ->
        ii.mapTo(mutableSetOf(), intToItem::get)
    }
}

data class CycleInfo<T>(
    val prefix: List<T>,
    val cycle: List<T>
) {
    val items: Iterable<T> get() = sequence {
        for (item in prefix) {
            yield(item)
        }
        for (item in cycle) {
            yield(item)
        }
    }.asIterable()

    operator fun get(idx: Int): T = when {
        idx < prefix.size -> prefix[idx]
        cycle.isEmpty() -> throw NoSuchElementException()
        else -> cycle[(idx - prefix.size).rem(cycle.size)]
    }
}

fun <T> Iterable<T>.findCycle(): CycleInfo<T> {
    val stateToIndex = mutableMapOf<T, Int>()
    val seen = mutableListOf<T>()
    for (item in this) {
        val existingIndex = stateToIndex.putIfAbsent(item, seen.size)
        if (existingIndex == null) {
            seen += item
        } else {
            return CycleInfo(seen.subList(0, existingIndex), seen.subList(existingIndex, seen.size))
        }
    }
    return CycleInfo(seen, emptyList())
}

fun <T> Sequence<T>.findCycle(): CycleInfo<T> = asIterable().findCycle()

fun gcd(a: Long, b: Long): Long {
    var aa = a
    var bb = b
    while (bb != 0L) {
        val t = bb
        bb = aa.rem(bb)
        aa = t
    }
    return aa
}

fun lcm(a: Int, b: Int): Int {
    val ll = gcd(a.toLong(), b.toLong())
    return a / ll.toInt() * b
}

fun lcm(a: Long, b: Long): Long {
    val ll = gcd(a, b)
    return a / ll * b
}
