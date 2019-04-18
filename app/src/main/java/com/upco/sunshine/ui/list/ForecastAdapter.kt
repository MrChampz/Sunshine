package com.upco.sunshine.ui.list

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.upco.sunshine.R
import com.upco.sunshine.data.database.ListWeatherEntry
import com.upco.sunshine.utilities.SunshineDateUtils
import com.upco.sunshine.utilities.SunshineWeatherUtils
import kotlinx.android.synthetic.main.list_item_forecast.view.*
import java.util.*
import kotlin.IllegalArgumentException

/**
 * Exposes a list of weather forecasts from a list of {@link WeatherEntry} to a {@link RecyclerView}.
 *
 * @param context      Used to talk to the UI and app resources.
 * @param clickHandler The on-click handler for this adapter. This single handler is called
 *                     when an item is clicked.
 */
class ForecastAdapter(val context: Context, val clickHandler: ForecastAdapterOnItemClickHandler):
        RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_TODAY = 0
        const val VIEW_TYPE_FUTURE_DAY = 1
    }

    /**
     * Flag to determine if we want to use a separate view for the list item that represents
     * today. This flag will be true when the phone is in portrait mode and false when the phone
     * is in landscape. This flag will be set by accessing boolean resources.
     */
    private val useTodayLayout = context.resources.getBoolean(R.bool.use_today_layout)
    private var forecast = listOf<ListWeatherEntry>()

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent   The ViewGroup that these ViewHolders are contained within.
     * @param viewType If your RecyclerView has more than one type of item (like ours does) you
     *                 can use this viewType integer to provide a different layout. See
     *                 {@link androidx.recyclerview.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                 for more details.
     * @return A new ViewHolder that holds the View for each list item.
     */
    @SuppressLint("NewApi")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = getLayoutIdByType(viewType)
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        view.isFocusable = true
        return ViewHolder(view)
    }

    /**
     * The onBindViewHolder method is called by the RecyclerView to display the data at the
     * specified position. In this method, we update the contents of the ViewHolder to display the
     * weather details for this particular position, using the "position" argument that is
     * conveniently passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the
     *                 contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(forecast[position])
    }

    /**
     * Returns an integer code related to the type of View we want the ViewHolder to be at a given
     * position. This method is useful when we want to use different layouts for different items
     * depending on their position. In Sunshine, we take advantage of this method to provide a
     * different layout for the "today" layout. The "today" layout is only shown in portrait mode
     * with the first item in the list.
     *
     * @param position Index within our RecyclerView and list.
     * @return The view type (today or future day).
     */
    override fun getItemViewType(position: Int): Int {
        return if (useTodayLayout && position == 0)
            VIEW_TYPE_TODAY
        else
            VIEW_TYPE_FUTURE_DAY
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast.
     */
    override fun getItemCount() = forecast.size

    /**
     * Swaps the list used by the ForecastAdapter for its weather data. This methods is called by
     * {@link MainActivity} after a load has finished. When this method is called, we assume we
     * have a new set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newForecast The new list of forecasts to use as ForecastAdapter's data source.
     */
    fun swapForecast(newForecast: List<ListWeatherEntry>) {
        // If there was no forecast data, then recreate all of the list
        if (forecast.isEmpty()) {
            forecast = newForecast
            notifyDataSetChanged()
        } else {
            /*
             * Otherwise we use DiffUtil to calculate the changes and update accordingly. This
             * shows the four methods you need to override to return a DiffUtil callback. The
             * old list is the current list stored in "forecast", where the new list is the new
             * values passed in from the observing the database.
             */

            val result = DiffUtil.calculateDiff(object: DiffUtil.Callback() {

                override fun getOldListSize() = forecast.size
                override fun getNewListSize() = newForecast.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return forecast[oldItemPosition].id == newForecast[newItemPosition].id
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val newWeather = newForecast[newItemPosition]
                    val oldWeather = forecast[oldItemPosition]

                    return newWeather.id == oldWeather.id && newWeather.date == oldWeather.date
                }
            })

            forecast = newForecast
            result.dispatchUpdatesTo(this)
        }
    }

    /**
     * Returns the layout id depending on whether the list item is a normal item or the larger
     * "today" list item.
     *
     * @param viewType The integer containing the view type id.
     * @return Return the corresponding layout id of the specified view type.
     */
    private fun getLayoutIdByType(viewType: Int): Int {
        return when (viewType) {
            VIEW_TYPE_TODAY -> R.layout.list_item_forecast_today
            VIEW_TYPE_FUTURE_DAY -> R.layout.list_item_forecast
            else -> throw IllegalArgumentException("Invalid view type, value of $viewType")
        }
    }

    /**
     * The interface that receives onItemClick messages.
     */
    interface ForecastAdapterOnItemClickHandler {
        fun onItemClick(date: Date)
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view), View.OnClickListener {

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onItemClick handler registered with this adapter, passing
         * that date.
         *
         * @param v The View that was clicked.
         */
        override fun onClick(v: View?) {
            val date = forecast[adapterPosition].date
            clickHandler.onItemClick(date)
        }

        /**
         * In this method, we update the contents of the ViewHolder to display the
         * weather details, using the "entry" argument that is conveniently passed into us.
         *
         * @param entry The ListWeatherEntry with the values to update the ViewHolder contents.
         */
        fun bind(entry: ListWeatherEntry) {
            /****************
             * Weather icon *
             ****************/
            val weatherIconId = entry.weatherIconId
            val resId = getImageResourceId(weatherIconId, adapterPosition)
            view.weather_icon.setImageResource(resId)

            /****************
             * Weather date *
             ****************/
            val dateInMillis = entry.date.time

            /* Get human readable string using our utility method */
            val dateString = SunshineDateUtils.getFriendlyDateString(context, dateInMillis, false)

            /* Display friendly date string */
            view.date.text = dateString

            /***********************
             * Weather description *
             ***********************/
            val description = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherIconId)

            /* Create the accessibility (a11y) string from the weather description */
            val descriptionA11y = context.getString(R.string.a11y_forecast, description)

            /* Set the text and content description (for accessibility purposes) */
            view.weather_description.text = description
            view.weather_description.contentDescription = descriptionA11y

            /**************************
             * High (max) temperature *
             **************************/
            val highInCelsius = entry.max

            /*
             * If the user's preference for weather is fahrenheit, formatTemperature will convert
             * the temperature. This method will also append either °C or °F to the temperature
             * string.
             */
            val highString = SunshineWeatherUtils.formatTemperature(context, highInCelsius)

            /* Create the accessibility (a11y) string from the weather description */
            val highA11y = context.getString(R.string.a11y_high_temp, highString)

            /* Set the text and content description (for accessibility purposes) */
            view.high_temperature.text = highString
            view.high_temperature.contentDescription = highA11y

            /*************************
             * Low (min) temperature *
             *************************/
            val lowInCelsius = entry.min

            /*
             * If the user's preference for weather is fahrenheit, formatTemperature will convert
             * the temperature. This method will also append either °C or °F to the temperature
             * string.
             */
            val lowString = SunshineWeatherUtils.formatTemperature(context, lowInCelsius)

            /* Create the accessibility (a11y) string from the weather description */
            val lowA11y = context.getString(R.string.a11y_low_temp, lowString)

            /* Set the text and content description (for accessibility purposes) */
            view.low_temperature.text = lowString
            view.low_temperature.contentDescription = lowA11y

            /** Set OnClickListener **/
            view.setOnClickListener(this)
        }

        /**
         * Converts the weather icon id from Open Weather to the local resource id. Returns the
         * correct image based on weather the forecast is for today (large image) or the
         * future(small image).
         *
         * @param weatherIconId Open Weather icon id.
         * @param position      Position in list.
         * @return Drawable image resource id for weather.
         */
        private fun getImageResourceId(weatherIconId: Int, position: Int): Int {
            val viewType = getItemViewType(position)
            return when (viewType) {
                VIEW_TYPE_TODAY -> {
                    SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherIconId)
                }
                VIEW_TYPE_FUTURE_DAY -> {
                    SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherIconId)
                }
                else -> throw IllegalArgumentException("Invalid view type, value of $viewType")
            }
        }
    }
}