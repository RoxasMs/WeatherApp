package com.example.weatherapp;

import static android.content.ContentValues.TAG;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "0003d3115fd24d6d15c28ea7188d92e2";  // Reemplaza con tu clave de API
    private static final String API_URL = "\"https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private static int HTTP_OK = 200;
    private static int HTTP_BAD_REQUEST = 404;
    private RequestQueue mRequestQueue;
    private Button weatherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherButton = findViewById(R.id.button_search);
        EditText cityInput = findViewById(R.id.city);
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = cityInput.getText().toString();
                if(cityName != "") {
                    getData(cityName);
                }
            }
        });

    }

    private void getData(String cityName) {
        // Verifica si ya hay una instancia de RequestQueue creada
        if (mRequestQueue == null) {
            // Si no hay una instancia, crea una nueva
            mRequestQueue = Volley.newRequestQueue(this);
        }

        String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", cityName, API_KEY);

        // JsonObjectRequest se utiliza para solicitudes de tipo JSON
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Código para procesar el JSON
                            int responseCode = (int) response.get("cod");
                            if (responseCode == HTTP_OK){
                                String[] data = parseWeatherJson(response);

                                String temp = data[0];
                                String skyState =  data[1];
                                String name = data[2];

                                TextView city_name_field = findViewById(R.id.city_name_field);
                                city_name_field.setText(name);

                                TextView state_sky_field = findViewById(R.id.state_sky_field);
                                state_sky_field.setText(skyState);

                                TextView c_temp_field = findViewById(R.id.c_temp_field);
                                c_temp_field.setText(temp);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.e(TAG, "Error en la solicitud: " + error.getMessage());

                    }
                }
        );
        mRequestQueue.add(request);
    }


    private String[] parseWeatherJson(JSONObject response) throws JSONException {
        JSONObject mainObject = (JSONObject) response.get("main");
        JSONArray weatherArray = (JSONArray) response.get("weather");
        JSONObject  weatherObject = (JSONObject) weatherArray.get(0);

        String currentTemp = new DecimalFormat("#.00 ºC").format((Double) mainObject.get("temp") - 273.15);
        String skyState = (String) weatherObject.get("main") + " : " + (String) weatherObject.get("description");
        String cityName = (String) response.get("name");
        return new String[]{ currentTemp, skyState, cityName};
    }
}

