package com.upco.sunshine.data.network

import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.upco.sunshine.utilities.InjectorUtils

class SunshineFirebaseJobService: JobService() {

    companion object {
        private val LOG_TAG = SunshineFirebaseJobService::class.java.simpleName
    }

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     * <p>
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return Whether there is more work remaining.
     */
    override fun onStartJob(job: JobParameters): Boolean {
        Log.d(LOG_TAG, "Job service started")

        val networkDataSource = InjectorUtils.provideNetworkDataSource(applicationContext)
        networkDataSource.fetchWeather()

        jobFinished(job, false)

        return true
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return Whether the job should be retried.
     * @see Job.Builder#setRetryStrategy(RetryStrategy)
     * @see RetryStrategy
     */
    override fun onStopJob(job: JobParameters) = true
}