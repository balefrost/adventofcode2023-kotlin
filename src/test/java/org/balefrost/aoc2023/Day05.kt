package org.balefrost.aoc2023

import org.junit.jupiter.api.Test
import kotlin.math.max
import kotlin.math.min

class Day05 {
    val sampleInput = """
        seeds: 79 14 55 13

        seed-to-soil map:
        50 98 2
        52 50 48

        soil-to-fertilizer map:
        0 15 37
        37 52 2
        39 0 15

        fertilizer-to-water map:
        49 53 8
        0 11 42
        42 0 7
        57 7 4

        water-to-light map:
        88 18 7
        18 25 70

        light-to-temperature map:
        45 77 23
        81 45 19
        68 64 13

        temperature-to-humidity map:
        0 69 1
        1 0 69

        humidity-to-location map:
        60 56 37
        56 93 4
    """.trimIndent()

    val lines: List<String>

    init {
//        val lines = sampleInput.lines().toMutableList()
        val lines = this::class.java.getResource("day05.txt")!!.readText().lines().toMutableList()
        if (lines.last().isBlank()) {
            lines.removeLast()
        }
        this.lines = lines
    }

    fun coalesceRanges(ranges: List<LongRange>): List<LongRange> {
        val sortedRanges = ranges.sortedBy { it.first }
        val result = mutableListOf<LongRange>()
        for (range in sortedRanges) {
            if (result.isEmpty()) {
                result += range
            } else if (range.first <= result.last().last + 1){
                result[result.lastIndex] = LongRange(result.last().first, range.last)
            } else {
                result += range
            }
        }
        return result
    }

    data class ParsedInput(val seeds: List<Long>, val maps: Map<String, CompactMap>)

    fun parse(lines: List<String>): ParsedInput {
        val seeds = lines[0].split(": ")[1].split(" ").map { it.toLong() }
        val byHeader = mutableMapOf<String, CompactMap>()
        val iter = lines.subList(2, lines.size).listIterator()
        while (iter.hasNext()) {
            val (hfrom, _, hto) = iter.next().split(" ")[0].split("-")
            val entries = mutableListOf<CompactMap.Entry>()
            while (iter.hasNext()) {
                val nextLine = iter.next()
                if (nextLine.isEmpty()) {
                    break
                }
                val (to, from, count) = nextLine.split(" ").map { it.toLong() }
                entries += CompactMap.Entry(from, to, count)
            }
            byHeader[hfrom] = CompactMap(entries, hfrom, hto)
        }
        return ParsedInput(seeds, byHeader)
    }

    class CompactMap(entries: List<Entry>, val keyType: String, val valueType: String) {
        private val entries = entries.sortedBy { it.keyRange.first }

        class Entry(keyStart: Long, valueStart: Long, count: Long) {
            val keyRange = keyStart..<keyStart + count
            private val valueRange get() = keyRange.first + delta .. keyRange.last + delta
            val delta = valueStart - keyStart

            override fun toString(): String {
                return "$keyRange: ${if (delta > 0) "+" else ""}$delta"
            }
        }

        override fun toString(): String {
            return entries.joinToString(", ")
        }

        fun lookup(key: Long): Long {
            val index = entries.binarySearch { entry ->
                when {
                    entry.keyRange.last < key -> -1
                    entry.keyRange.first > key -> 1
                    else -> 0
                }
            }
            if (index < 0) {
                return key
            }
            val entry = entries[index]
            return key + entry.delta
        }

        fun lookupAll(range: LongRange): List<LongRange> {
            val lowerBound = entries.binarySearch { entry ->
                if (entry.keyRange.last < range.first) {
                    -1
                } else {
                    1
                }
            }.inv()

            if (lowerBound == entries.size) {
                return listOf(range)
            }

            val result = mutableListOf<LongRange>()

            var nextExpectedOutput = range.first
            for (i in lowerBound..entries.lastIndex) {
                val entry = entries[i]

                if (entry.keyRange.first > range.last) {
                    break
                }

                // fill in any gaps before `entry`
                if (entry.keyRange.first > nextExpectedOutput) {
                    result += LongRange(nextExpectedOutput, entry.keyRange.first - 1)
                }

                val newKeyStart = max(entry.keyRange.first, range.first)
                val newKeyEnd = min(entry.keyRange.last, range.last)
                result += LongRange(newKeyStart + entry.delta, newKeyEnd + entry.delta)
                nextExpectedOutput = newKeyEnd + 1
            }

            if (nextExpectedOutput <= range.last) {
                result += LongRange(nextExpectedOutput, range.last)
            }

            return result
        }
    }

    @Test
    fun part01() {
        val input = parse(lines)
        val locations = input.seeds.map { seed ->
            generateSequence(seed to "seed") { (number, type) ->
                val m = input.maps[type]
                if (m != null) {
                    m.lookup(number) to m.valueType
                } else {
                    null
                }
            }.last().first
        }
        println(locations.min())
    }

    @Test
    fun part02() {
        val input = parse(lines)
        val seedRanges = input.seeds.chunked(2).map { (a, b) -> LongRange(a, a + b - 1) }
        val chained = generateSequence(seedRanges to "seed") { (ranges, type) ->
            val other = input.maps[type]
            if (other == null) {
                null
            } else {
                val newRanges = coalesceRanges(ranges.flatMap {
                    other.lookupAll(it)
                })
                newRanges to other.valueType
            }
        }.toList()

        val (lastRanges, _) = chained.last()
        println(lastRanges.first().first)
    }
}
