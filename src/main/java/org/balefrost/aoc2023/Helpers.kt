package org.balefrost.aoc2023

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
}

data class Region2D(val x: Int, val y: Int, val w: Int, val h: Int) {
    operator fun contains(pt: Point2D): Boolean {
        return pt.x >= x && pt.x < x + w && pt.y >= y && pt.y < y + h
    }
}
