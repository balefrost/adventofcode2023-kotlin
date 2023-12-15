package org.balefrost.aoc2023

import org.junit.jupiter.api.Test

class Day15 {
    val sampleText = """
        rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7
    """.trimIndent()

    val textFromFile = this::class.java.getResource("day15.txt")!!.readText()

//    val inputText = sampleText
    val inputText = textFromFile

    val lines = inputText.lines().dropLastWhile { it.isEmpty() }

    val steps = lines.first().split(",")

    fun hash(s: String): Int {
        fun updateHash(acc: Int, ch: Char): Int {
            return ((acc + ch.code) * 17).mod(256)
        }

        return s.fold(0, ::updateHash)
    }

    @Test
    fun part01() {
        println(steps.map(::hash).sum())
    }

    data class Step(val label: String, val op: Char, val focalLength: Int) {
        override fun toString(): String {
            val focalLengthStr = if (op == '-') "" else focalLength.toString()
            return "$label$op$focalLengthStr"
        }
    }

    class BoxContent {
        private class Node(val label: String, var focalLength: Int, var prev: Node?, var next: Node?) {
            override fun toString(): String {
                return "[${label} ${focalLength}]"
            }
        }
        private var first: Node? = null
        private var last: Node? = null
        private val lookup = mutableMapOf<String, Node>()

        override fun toString(): String {
            return generateSequence(first) { it.next }.map { it.toString() }.joinToString(" ")
        }

        fun addLens(label: String, focalLength: Int) {
            val existingNode = lookup[label]
            if (existingNode != null) {
                existingNode.focalLength = focalLength
            } else {
                val newNode = Node(label, focalLength, null, null)
                lookup[label] = newNode
                if (last == null) {
                    last = newNode
                    first = last
                } else {
                    newNode.prev = last
                    last!!.next = newNode
                    last = newNode
                }
            }
        }

        fun removeLens(label: String) {
            val existing = lookup.remove(label)
            if (existing != null) {
                if (existing.prev != null) {
                    existing.prev?.next = existing.next
                } else {
                    first = existing.next
                }

                if (existing.next != null) {
                    existing.next?.prev = existing.prev
                } else {
                    last = existing.prev
                }
            }
        }

        val isEmpty: Boolean get() = lookup.isEmpty()
        val isNotEmpty: Boolean get() = !isEmpty

        val lenses: Sequence<Int> get() = sequence {
            var current = first
            while (current != null) {
                yield(current.focalLength)
                current = current.next
            }
        }
    }

    fun scoreBoxContent(boxNo: Int, content: BoxContent): Int {
        return content.lenses.withIndex().map { (index, focalLength) -> (boxNo + 1) * (index + 1) * focalLength }.sum()
    }
    @Test
    fun part02() {
        val steps2 = steps.map {
            val splitIdx = it.indexOfAny(charArrayOf('-', '='))
            val label = it.substring(0, splitIdx)
            val op = it[splitIdx]
            val focalLength = if (splitIdx == it.lastIndex) -1 else it.substring(splitIdx + 1).toInt()
            Step(label, op, focalLength)
        }

        val boxes = (0..255).map { BoxContent() }

        for (step in steps2) {
            val hash = hash(step.label)
            when (step.op) {
                '=' -> {
                    boxes[hash].addLens(step.label, step.focalLength)
                }
                '-' -> {
                    boxes[hash].removeLens(step.label)
                }
            }
        }

        val result = boxes.mapIndexed { index, boxContent -> scoreBoxContent(index, boxContent) }.sum()

        println(result)
    }

}