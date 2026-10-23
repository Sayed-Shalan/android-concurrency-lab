package com.sayedshalan.lab.concurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.cancellation.CancellationException

class SamplesTest {
    @Test
    fun threadKeepsRunningUntilJoined() {
        val release = CountDownLatch(1)
        val done = AtomicBoolean(false)
        val handle =
            ThreadHandle {
                release.await()
                done.set(true)
            }
        handle.start()
        assertFalse(done.get())
        release.countDown()
        handle.join()
        assertTrue(done.get())
    }

    @Test
    fun scopeWaitsForTheChildBeforeReturning() =
        runTest {
            var done = false
            workInsideScope { done = true }
            assertTrue(done)
        }

    @Test
    fun withContextMovesToTheDefaultDispatcher() =
        runTest {
            val name = currentContextName(Dispatchers.Default)
            assertTrue(name.contains("DefaultDispatcher"))
        }

    @Test
    fun cancellingTheParentRunsTheChildFinallyBlock() =
        runTest {
            var cleaned = false
            cancelChild { cleaned = true }
            assertTrue(cleaned)
        }

    @Test
    fun aFailingChildCancelsItsSibling() =
        runTest {
            var siblingFinished = false
            val error =
                runCatching {
                    siblingIsCancelledWhenChildFails { siblingFinished = true }
                }.exceptionOrNull()
            assertTrue(error is IllegalStateException)
            assertEquals("boom", error?.message)
            assertFalse(siblingFinished)
        }

    @Test
    fun supervisorLetsTheSiblingFinish() =
        runTest {
            var siblingFinished = false
            val failure = siblingContinuesUnderSupervisor { siblingFinished = true }
            assertTrue(siblingFinished)
            assertTrue(failure is IllegalStateException)
        }

    @Test
    fun valueOrNullDoesNotSwallowCancellation() =
        runTest {
            val job =
                launch {
                    valueOrNull {
                        throw CancellationException("stop")
                    }
                }
            job.join()
            assertTrue(job.isCancelled)
            val value = valueOrNull { throw IllegalStateException("nope") }
            assertNull(value)
        }
}
