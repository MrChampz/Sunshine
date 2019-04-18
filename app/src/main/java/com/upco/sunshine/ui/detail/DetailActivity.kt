package com.upco.sunshine.ui.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.upco.sunshine.R
import com.upco.sunshine.data.database.WeatherEntry
import com.upco.sunshine.databinding.ActivityDetailBinding
import com.upco.sunshine.utilities.InjectorUtils
import com.upco.sunshine.utilities.SunshineDateUtils
import com.upco.sunshine.utilities.SunshineWeatherUtils
import java.util.*

/**
 * Displays single day's forecast.
 */
class DetailActivity: AppCompatActivity() {

    companion object {
        const val WEATHER_ID_EXTRA = "WEATHER_ID_EXTRA"
    }

    private lateinit var detailBinding: ActivityDetailBinding
    private lateinit var viewModel: DetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        setupViewModel()
    }

    /**
     * This method setup the DataBinding.
     */
    private fun setupDataBinding() {
        // Bind to the DataBinding.
        detailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
    }

    /**
     * This method setup the ViewModel.
     */
    private fun setupViewModel() {
        // Get the timestamp and transform to actual date.
        val timestamp = intent.getLongExtra(WEATHER_ID_EXTRA, -1)
        val date = Date(timestamp)

        // Get the ViewModel from the factory
        val factory = InjectorUtils.provideDetailViewModelFactory(applicationContext, date)
        viewModel = ViewModelProviders.of(this, factory).get(DetailViewModel::class.java)

        // Observers changes in the WeatherEntry with the id
        viewModel.weather.observe(this, Observer<WeatherEntry> {
            // If the weather forecast details change, update the UI
            if (it != null) bindWeatherToUI(it)
        })
    }

    /**
     * This method binds the weather information to the UI.
     */
    private fun bindWeatherToUI(weatherEntry: WeatherEntry) {
        /****************
         * Weather icon *
         ****************/

        val weatherId = weatherEntry.weatherIconId
        val weatherImgId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId)

        /* Set the resource id on the icon to display the art */
        detailBinding.primaryInfo.weatherIcon.setImageResource(weatherImgId)

        /****************
         * Weather date *
         ****************/

        /*
         * The date that is stored is a GMT representation at midnight of the date when the
         * weather information was loaded for.
         *
         * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
         * the date representation for the local date in local time.
         * SunshineDateUtils#getFriendlyDateString takes care of this for us.
         */
        val localDateMidnightGmt = weatherEntry.date.time
        val dateText = SunshineDateUtils.getFriendlyDateString(
            this, localDateMidnightGmt,
            true
        ).capitalize()
        detailBinding.primaryInfo.date.text = dateText

        /***********************
         * Weather description *
         ***********************/

        /* Use the weatherId to obtain the proper description */
        val description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId)

        /* Create accessibility (a11y) string from the weather description */
        val descriptionA11y = getString(R.string.a11y_forecast,  description)

        /* Set the text and content description (for accessibility purposes) */
        detailBinding.primaryInfo.weatherDescription.text = description
        detailBinding.primaryInfo.weatherDescription.contentDescription = descriptionA11y

        /* Set the content description on the weather image (for accessibility purposes) */
        detailBinding.primaryInfo.weatherIcon.contentDescription = descriptionA11y

        /**************************
         * High (max) temperature *
         **************************/
        val highInCelsius = weatherEntry.max

        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * string.
         */
        val highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius)

        /* Create the accessibility (a11y) string from the weather description */
        val highA11y = getString(R.string.a11y_high_temp, highString)

        /* Set the text and content description (for accessibility purposes) */
        detailBinding.primaryInfo.highTemperature.text = highString
        detailBinding.primaryInfo.highTemperature.contentDescription = highA11y

        /*************************
         * Low (min) temperature *
         *************************/
        val lowInCelsius = weatherEntry.min

        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * string.
         */
        val lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius)

        /* Create the accessibility (a11y) string from the weather description */
        val lowA11y = getString(R.string.a11y_low_temp, lowString)

        /* Set the text and content description (for accessibility purposes) */
        detailBinding.primaryInfo.lowTemperature.text = lowString
        detailBinding.primaryInfo.lowTemperature.contentDescription = lowA11y

        /************
         * Humidity *
         ************/

        val humidity = weatherEntry.humidity
        val humidityString = getString(R.string.format_humidity, humidity)
        val humidityA11y = getString(R.string.a11y_humidity, humidityString)

        /* Set the text and content description (for accessibility purposes) */
        detailBinding.extraDetails.humidity.text = humidityString
        detailBinding.extraDetails.humidity.contentDescription = humidityA11y
        detailBinding.extraDetails.humidityLabel.contentDescription = humidityA11y

        /****************************
         * Wind speed and direction *
         ****************************/

        /* Read wind speed (in MPH) and direction (in compass degrees) */
        val windSpeed = weatherEntry.wind
        val windDirection = weatherEntry.degrees
        val windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection)
        val windA11y = getString(R.string.a11y_wind, windString)

        /* Set the text and content description (for accessibility purposes) */
        detailBinding.extraDetails.wind.text = windString
        detailBinding.extraDetails.wind.contentDescription = windA11y
        detailBinding.extraDetails.windLabel.contentDescription = windA11y

        /************
         * Pressure *
         ************/

        val pressure = weatherEntry.pressure

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        val pressureString = getString(R.string.format_pressure, pressure)
        val pressureA11y = getString(R.string.a11y_pressure, pressureString)

        /* Set the text and content description (for accessibility purposes) */
        detailBinding.extraDetails.pressure.text = pressureString
        detailBinding.extraDetails.pressure.contentDescription = pressureA11y
        detailBinding.extraDetails.pressureLabel.contentDescription = pressureA11y
    }
}