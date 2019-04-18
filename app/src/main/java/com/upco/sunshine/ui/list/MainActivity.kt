package com.upco.sunshine.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.upco.sunshine.R
import com.upco.sunshine.data.database.ListWeatherEntry
import com.upco.sunshine.ui.detail.DetailActivity
import com.upco.sunshine.utilities.InjectorUtils
import kotlinx.android.synthetic.main.activity_forecast.*
import java.util.*

/**
 * Displays a list of the next 14 days of forecasts.
 */
class MainActivity: AppCompatActivity(), ForecastAdapter.ForecastAdapterOnItemClickHandler {

    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var viewModel: MainViewModel
    private var position = RecyclerView.NO_POSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        setupRecyclerView()
        setupViewModel()
    }

    /**
     * This method is for responding to clicks from our list.
     *
     * @param date Date of forecast.
     */
    override fun onItemClick(date: Date) {
        val timestamp = date.time
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.WEATHER_ID_EXTRA, timestamp)
        startActivity(intent)
    }

    /**
     * This method setup the RecyclerView.
     */
    private fun setupRecyclerView() {
        /* Create and associate a LayoutManager. */
        rv_forecast.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView.
         */
        rv_forecast.setHasFixedSize(true)

        /*
         * The ForecastAdapter is responsible for linking our weather data with Views that
         * will end up displaying our weather data.
         *
         * Although passing in "this" twice may seem strange, it its actually a sign of separation
         * of concerns, which is best programming practice. The ForecastAdapter requires an
         * Android Context (which all Activities are) as well as an onClickHandler. Since our
         * MainActivity implements the ForecastAdapter ForecastOnClickHandler interface, "this"
         * is also an instance of that type of handler.
         */
        forecastAdapter = ForecastAdapter(this, this)

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        rv_forecast.adapter = forecastAdapter
    }

    /**
     * This method setup the ViewModel.
     */
    private fun setupViewModel() {
        val factory = InjectorUtils.provideMainViewModelFactory(applicationContext)
        viewModel = ViewModelProviders.of(this, factory).get(MainViewModel::class.java)

        viewModel.forecast.observe(this, Observer<List<ListWeatherEntry>> {
            forecastAdapter.swapForecast(it)

            if (position == RecyclerView.NO_POSITION)
                position = 0

            rv_forecast.smoothScrollToPosition(position)

            // Show the weather list or the loading screen based on weather the forecast data
            // exists and is loaded.
            if (it != null && it.isNotEmpty())
                showWeatherDataView()
            else
                showLoading()
        })
    }

    /**
     * This method will make the View for the weather data visible and hide the error message and
     * loading indicator.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each View is currently visible or invisible.
     */
    private fun showWeatherDataView() {
        // First, hide the loading indicator
        pb_loading_indicator.visibility = View.INVISIBLE
        // Finally, make sure the weather data is visible
        rv_forecast.visibility = View.VISIBLE
    }

    /**
     * This method will make the loading indicator visible and hide the weather View and error
     * message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't need to check whether
     * each View is currently visible or invisible.
     */
    private fun showLoading() {
        // Then, hide the weather data
        rv_forecast.visibility = View.INVISIBLE
        // Finally, show the loading indicator
        rv_forecast.visibility = View.VISIBLE
    }
}