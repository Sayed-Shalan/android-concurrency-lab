package com.sayedshalan.lab.concurrency

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * A thread keeps running after the caller returns. The caller has to join it.
 * A coroutine launched in [coroutineScope] is joined before the scope returns.
 */
class ThreadHandle(
    block: () -> Unit,
) {
    private val thread = Thread(block)

    fun start() {
        thread.start()
    }

    fun join() {
        thread.join()
    }
}

suspend fun workInsideScope(markDone: () -> Unit) {
    coroutineScope {
        launch {
            delay(1_000)
            markDone()
        }
    }
}

suspend fun currentContextName(context: CoroutineContext): String =
    withContext(context) {
        Thread.currentThread().name
    }

suspend fun cancelChild(onCleanup: () -> Unit) {
    coroutineScope {
        val started = CompletableDeferred<Unit>()
        val parent =
            launch {
                launch {
                    try {
                        started.complete(Unit)
                        delay(60_000)
                    } finally {
                        onCleanup()
                    }
                }
            }
        started.await()
        parent.cancel()
        parent.join()
    }
}

suspend fun siblingIsCancelledWhenChildFails(onSiblingDone: () -> Unit) {
    coroutineScope {
        launch {
            delay(60_000)
            onSiblingDone()
        }
        launch {
            throw IllegalStateException("boom")
        }
    }
}

suspend fun siblingContinuesUnderSupervisor(onSiblingDone: () -> Unit): Throwable? {
    var failure: Throwable? = null
    val handler = CoroutineExceptionHandler { _, error -> failure = error }
    supervisorScope {
        launch(handler) {
            throw IllegalStateException("boom")
        }
        launch {
            onSiblingDone()
        }
    }
    return failure
}

suspend fun valueOrNull(block: suspend () -> String): String? {
    return try {
        block()
    } catch (error: Throwable) {
        if (error is CancellationException) {
            throw error
        }
        null
    }
}
