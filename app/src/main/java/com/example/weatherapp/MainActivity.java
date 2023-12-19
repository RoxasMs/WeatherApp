package com.example.weatherapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "A";  // Reemplaza con tu clave de API
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=Madrid&appid=" + API_KEY;

    private Button weatherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherButton = findViewById(R.id.button_search);

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchWeatherTask().doInBackground();
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                // Hacer la solicitud a la API
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.setRequestProperty("accept","application/json");
                InputStream responseStream = urlConnection.getInputStream();
                return responseStream.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                // Procesar la respuesta JSON
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject coord = jsonResponse.getJSONObject("coord");
                    double lat = coord.getDouble("lat");
                    double lon = coord.getDouble("lon");
                    showCoordinates(lat, lon);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error al procesar la respuesta JSON", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Error en la solicitud", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showCoordinates(double lat, double lon) {
        String message = "Latitud: " + lat + "\nLongitud: " + lon;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

