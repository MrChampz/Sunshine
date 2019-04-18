package com.upco.sunshine

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Global executor pools for the whole application.
 * <p>
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait
 * behind webservice requests).
 */
class AppExecutors private constructor(
    val diskIO: Executor,
    val networkIO: Executor,
    val mainThread: Executor
) {

    companion object {
        // For singleton instantiation
        private val LOCK = Any()
        @Volatile private var instance: AppExecutors? = null

        operator fun invoke() = instance ?: synchronized(LOCK) {
            instance ?: AppExecutors(
                Executors.newSingleThreadExecutor(),
                Executors.newFixedThreadPool(3),
                MainThreadExecutor()
            ).also { instance = it }
        }

        private class MainThreadExecutor: Executor {
            private val mainThreadHandler = Handler(Looper.getMainLooper())

            override fun execute(command: Runnable?) {
                mainThreadHandler.post(command)
            }
        }
    }
}