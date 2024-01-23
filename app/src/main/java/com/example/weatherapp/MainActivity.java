package com.example.weatherapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothSocketException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "0003d3115fd24d6d15c28ea7188d92e2";  // Reemplaza con tu clave de API
    private static final String API_URL = "\"https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s";
    private static int HTTP_OK = 200;
    private static int HTTP_BAD_REQUEST = 404;
    private RequestQueue mRequestQueue;
    private Button weatherButton;
    private FloatingActionButton bluetoothButton;
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket socket;

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
                hideKeyboard();
                if ((cityName != null && !cityName.isEmpty())) {
                    getData(cityName);
                } else {
                    showSnackbar("Put some city name");
                }
            }
        });

        bluetoothButton = findViewById(R.id.bluetoothButton);
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            @Override
            public void onClick(View v) {
                try {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                    if (ActivityCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBluetoothIntent, 1);
                    }
                    if (socket != null && socket.isConnected()) {
                        showDisconnectDialog(v);
                    } else {
                        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                        for (BluetoothDevice device : bondedDevices) {
                            if (device.getName().equals("HMSoft")) {
                                TextView lum_field, temp_field;

                                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                                socket.connect();

                                findViewById(R.id.amb_lum_label).setVisibility(View.VISIBLE);
                                findViewById(R.id.amb_temp_label).setVisibility(View.VISIBLE);

                                lum_field = findViewById(R.id.amb_lum_field);
                                temp_field = findViewById(R.id.amb_temp_field);

                                lum_field.setVisibility(View.VISIBLE);
                                temp_field.setVisibility(View.VISIBLE);

                                new BluetoothAsyncTask(socket, lum_field, temp_field).execute();
                            }
                        }
                    }

                    } catch(BluetoothSocketException e){
                        showSnackbar("Error de Bluetooth");
                    } catch(IOException e){
                        showSnackbar("Error de Conexión");
                    }
            }
        });

    }

    private void showDisconnectDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setMessage("¿Desea cerrar la conexión Bluetooth?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            if (socket != null) {
                                socket.close();
                            }
                            // Oculta las vistas relacionadas con la conexión
                            findViewById(R.id.amb_lum_label).setVisibility(View.GONE);
                            findViewById(R.id.amb_temp_label).setVisibility(View.GONE);
                            findViewById(R.id.amb_lum_field).setVisibility(View.GONE);
                            findViewById(R.id.amb_temp_field).setVisibility(View.GONE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            showSnackbar("Error al cerrar la conexión Bluetooth");
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // No hace nada si el usuario elige no cerrar la conexión
                    }
                });
        builder.create().show();
    }

    private void getData(String cityName) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(this);
        }

        String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s", cityName, API_KEY);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int responseCode = (int) response.get("cod");
                            if (responseCode == HTTP_OK){
                                String[] data = parseWeatherJson(response);

                                String temp = data[0];
                                String skyState =  data[1];
                                String name = data[2];

                                findViewById(R.id.city_name_label).setVisibility(View.VISIBLE);
                                findViewById(R.id.state_sky_label).setVisibility(View.VISIBLE);
                                findViewById(R.id.current_temperature_label).setVisibility(View.VISIBLE);

                                TextView city_name_field = findViewById(R.id.city_name_field);
                                city_name_field.setVisibility(View.VISIBLE);;
                                city_name_field.setText(name);

                                TextView state_sky_field = findViewById(R.id.state_sky_field);
                                state_sky_field.setVisibility(View.VISIBLE);
                                state_sky_field.setText(skyState);

                                TextView c_temp_field = findViewById(R.id.c_temp_field);
                                c_temp_field.setVisibility(View.VISIBLE);
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
                        showSnackbar("City don't Found");
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

    private void showSnackbar(String text) {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                text,
                Snackbar.LENGTH_SHORT
        );
        snackbar.show();
    }


    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }


}

