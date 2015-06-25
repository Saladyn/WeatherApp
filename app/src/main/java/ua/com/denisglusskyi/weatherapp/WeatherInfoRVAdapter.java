package ua.com.denisglusskyi.weatherapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Saladyn on 25.06.2015.
 */
public class WeatherInfoRVAdapter extends RecyclerView.Adapter<WeatherInfoRVAdapter.WeatherInfoViewHolder> {

    private List<Weather> weatherList;
    private Context context;

    public WeatherInfoRVAdapter(List<Weather> weatherList, Context context) {
        this.weatherList = weatherList;
        this.context = context;
    }

    @Override
    public WeatherInfoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.weather_card_item, viewGroup, false);
        WeatherInfoViewHolder viewHolder = new WeatherInfoViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WeatherInfoViewHolder holder, int position) {
        String url = buildImageUrl(weatherList.get(position).getIconUrl());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");

        holder.date.setText(sdf.format(weatherList.get(position).getDate()));
        holder.minTemperature.setText(String.valueOf(weatherList.get(position).getMinTemperature()));
        holder.maxTemperature.setText(String.valueOf(weatherList.get(position).getMaxTemperature()));
        holder.pressure.setText(String.valueOf(weatherList.get(position).getPressure()));
        holder.humidity.setText(String.valueOf(weatherList.get(position).getHumidity()));
        holder.windSpeed.setText(String.valueOf(weatherList.get(position).getWindSpeed()));
        Picasso.with(context).load(url).into(holder.weatherImage);
    }

    private String buildImageUrl(String fileName) {
        final String BASE_URL = "http://openweathermap.org/img/w/";
        final String EXTENSION = ".png";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BASE_URL);
        stringBuilder.append(fileName);
        stringBuilder.append(EXTENSION);

        return stringBuilder.toString();
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }

    class WeatherInfoViewHolder extends RecyclerView.ViewHolder {
        private TextView date;
        private TextView minTemperature;
        private TextView maxTemperature;
        private TextView pressure;
        private TextView humidity;
        private TextView windSpeed;
        private ImageView weatherImage;

        public WeatherInfoViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.textView_date);
            minTemperature = (TextView) itemView.findViewById(R.id.textView_minTemp);
            maxTemperature = (TextView) itemView.findViewById(R.id.textView_maxTemp);
            pressure = (TextView) itemView.findViewById(R.id.textView_pressure);
            humidity = (TextView) itemView.findViewById(R.id.textView_humidity);
            windSpeed = (TextView) itemView.findViewById(R.id.textView_windSpeed);
            weatherImage = (ImageView) itemView.findViewById(R.id.weather_image);
        }
    }
}
