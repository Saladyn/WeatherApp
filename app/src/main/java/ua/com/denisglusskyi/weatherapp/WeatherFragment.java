package ua.com.denisglusskyi.weatherapp;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class WeatherFragment extends Fragment implements OnMapReadyCallback, TextView.OnEditorActionListener {
    TextView mEnterCity;
    private List<Weather> mWeatherList;
    private WeatherInfoRVAdapter mAdapter;
    private String mLocation;
    private SupportMapFragment mMapFragment;
    private LatLng coords;
    private OnMapReadyCallback mOnMapReadyCallback;
    private EditText mSearch;

    public WeatherFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeatherList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        mEnterCity = (TextView) view.findViewById(R.id.textView_enterCity);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler_weather);
        rv.setHasFixedSize(false);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        mAdapter = new WeatherInfoRVAdapter(mWeatherList, getActivity());
        rv.setAdapter(mAdapter);

        mMapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        mOnMapReadyCallback = this;

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        View actionBarView = inflater.inflate(R.layout.search_layout, null);
        actionBar.setCustomView(actionBarView);
        mSearch = (EditText) actionBar.getCustomView().findViewById(R.id.city_search);
        mSearch.setOnEditorActionListener(this);
        actionBar.setDisplayShowCustomEnabled(true);

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.clear();
        map.setMyLocationEnabled(true);
        if (coords != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 6));
            map.addMarker(new MarkerOptions()
                    .title(mLocation)
                    .position(coords));
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        GetWeather getWeather = new GetWeather();
        getWeather.execute(textView.getText().toString());
        return true;
    }

    private class GetWeather extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = GetWeather.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {
            String jsonStr;
            if (params.length == 0) {
                jsonStr = "";
                return jsonStr;
            }

            URL url = buildURL(params[0]);
            HttpURLConnection urlConnection = getConnection(url);
            jsonStr = getDataFromServer(urlConnection);

            return jsonStr;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            mWeatherList.clear();
            if (jsonString != null && jsonString.length() > 0) {
                try {
                    mWeatherList.addAll(getWeatherDataFromString(jsonString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mAdapter.notifyDataSetChanged();

            if (mWeatherList.size() > 0) {
                mEnterCity.setVisibility(View.GONE);
                mMapFragment.getMapAsync(mOnMapReadyCallback);
            } else {
                mEnterCity.setVisibility(View.VISIBLE);
            }
        }

        private URL buildURL(String locationName) {
            final String API_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            String format = "json";
            String units = "metric";
            int numDays = 7;

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, locationName)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, String.valueOf(numDays))
                    .build();

            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            try {
                return new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private HttpURLConnection getConnection(URL url) {
            HttpURLConnection urlConnection = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return urlConnection;
        }

        private String getDataFromServer(HttpURLConnection connection) {
            BufferedReader reader = null;
            InputStream inputStream = null;
            try {
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            if (buffer.length() == 0) {
                return null;
            }

            return buffer.toString();
        }

        private ArrayList<Weather> getWeatherDataFromString(String jsonStr) throws JSONException {
            final String LIST_FIELD = "list";
            final String DATE_FIELD = "dt";
            final String TEMPERATURE_FIELD = "temp";
            final String MIN_TEMPERATURE_FIELD = "min";
            final String MAX_TEMPERATURE_FIELD = "max";
            final String PRESSURE_FIELD = "pressure";
            final String HUMIDITY_FIELD = "humidity";
            final String WEATHER_FIELD = "weather";
            final String ICON_URL_FIELD = "icon";
            final String WIND_SPEED_FIELD = "speed";
            final String CITY_FIELD = "city";
            final String COORD_FIELD = "coord";
            final String LON_FIELD = "lon";
            final String LAT_FIELD = "lat";

            ArrayList<Weather> weathers = new ArrayList<>();

            JSONObject weatherJson = new JSONObject(jsonStr);

            JSONObject coordsJson = weatherJson.getJSONObject(CITY_FIELD).getJSONObject(COORD_FIELD);
            coords = new LatLng(coordsJson.getDouble(LAT_FIELD), coordsJson.getDouble(LON_FIELD));

            JSONArray weatherArray = weatherJson.getJSONArray(LIST_FIELD);

            for (int i = 0; i < weatherArray.length(); i++) {
                Weather weather = new Weather();
                JSONObject dayObject = weatherArray.getJSONObject(i);
                Date date = dateInMillis(dayObject.getLong(DATE_FIELD));
                weather.setDate(date);

                weather.setPressure(dayObject.getDouble(PRESSURE_FIELD));
                weather.setHumidity(dayObject.getDouble(HUMIDITY_FIELD));
                weather.setWindSpeed(dayObject.getDouble(WIND_SPEED_FIELD));

                JSONObject temperatureObject = dayObject.getJSONObject(TEMPERATURE_FIELD);
                weather.setMinTemperature(temperatureObject.getDouble(MIN_TEMPERATURE_FIELD));
                weather.setMaxTemperature(temperatureObject.getDouble(MAX_TEMPERATURE_FIELD));

                JSONObject weatherObject = dayObject.getJSONArray(WEATHER_FIELD).getJSONObject(0);
                weather.setIconUrl(weatherObject.getString(ICON_URL_FIELD));
                weathers.add(weather);
            }
            return weathers;
        }

        private Date dateInMillis(long date) {
            final long MILLISECONDS_IN_SECOND = 1000;
            return new Date(date * MILLISECONDS_IN_SECOND);
        }
    }

}
