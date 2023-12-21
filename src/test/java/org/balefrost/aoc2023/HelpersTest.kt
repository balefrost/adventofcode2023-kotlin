package org.balefrost.aoc2023

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HelpersTest {

    @Test
    fun `findCycles finds a cycle`() {
        val cycleInfo = listOf(1, 2, 3, 4, 5, 3, 4, 5, 3, 4, 5).findCycle()
        assertThat(cycleInfo, equalTo(CycleInfo(listOf(1, 2), listOf(3, 4, 5))))
    }

    @Test
    fun `findCycles finds no cycle`() {
        val cycleInfo = listOf(1, 2, 3, 4, 5).findCycle()
        assertThat(cycleInfo, equalTo(CycleInfo(listOf(1, 2, 3, 4, 5), emptyList())))
    }

    @Test
    fun `findCycles allows lookup by index`() {
        val list = listOf(1, 2, 3, 4, 5, 3, 4, 5, 3, 4, 5)
        val cycleInfo = list.findCycle()
        assertThat(list.indices.map { cycleInfo[it] }, equalTo(list))
    }

    @Test
    fun `findCycles disallows indexing beyong the end of a non-cycle`() {
        val cycleInfo = listOf(1, 2, 3, 4, 5).findCycle()
        assertThrows<NoSuchElementException> { cycleInfo[10] }
    }
}