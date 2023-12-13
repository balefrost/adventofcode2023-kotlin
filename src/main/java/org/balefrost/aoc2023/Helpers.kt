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

