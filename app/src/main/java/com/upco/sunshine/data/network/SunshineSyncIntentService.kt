package com.upco.sunshine.data.network

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.upco.sunshine.utilities.InjectorUtils

/**
 * An {@link IntentService} subclass for immediately scheduling a sync with the server off of the
 * main thread. This is necessary because {@link com.firebase.jobdispatcher.FirebaseJobDispatcher}
 * will not trigger a job immediately. This should only be called when the application is on the
 * screen.
 */
class SunshineSyncIntentService: IntentService("SunshineSyncIntentService") {

    companion object {
        private val LOG_TAG = SunshineSyncIntentService::class.java.simpleName
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(LOG_TAG, "Intent service started")
        val networkDataSource = InjectorUtils.provideNetworkDataSource(applicationContext)
        networkDataSource.fetchWeather()
    }
}