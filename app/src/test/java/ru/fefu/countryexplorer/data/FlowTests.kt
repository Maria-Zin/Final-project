package ru.fefu.countryexplorer.data

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowTests {

    @Test
    fun `distinctUntilChanged removes duplicates`() = runTest {
        flowOf(1, 1, 2, 2, 3, 3, 1, 1).distinctUntilChanged().test {
            assertEquals(1, awaitItem())
            assertEquals(2, awaitItem())
            assertEquals(3, awaitItem())
            assertEquals(1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `combine merges flows`() = runTest {
        val f1 = MutableStateFlow("A")
        val f2 = MutableStateFlow(1)
        val f3 = MutableStateFlow(true)
        combine(f1, f2, f3) { v1, v2, v3 -> Triple(v1, v2, v3) }.test {
            assertEquals(Triple("A", 1, true), awaitItem())
            f1.value = "B"
            assertEquals(Triple("B", 1, true), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `map and filter transform flow`() = runTest {
        flowOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .filter { it % 2 == 0 }
            .map { it * 10 }
            .test {
                assertEquals(20, awaitItem())
                assertEquals(40, awaitItem())
                assertEquals(60, awaitItem())
                assertEquals(80, awaitItem())
                assertEquals(100, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun `retry recovers after error`() = runTest {
        var attempt = 0
        flow {
            if (attempt++ == 0) throw Exception("Fail")
            emit("Success")
        }.retry(1).test {
            assertEquals("Success", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `stateFlow emits current value`() = runTest {
        val sf = MutableStateFlow("initial")
        sf.value = "updated"
        sf.test {
            assertEquals("updated", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}