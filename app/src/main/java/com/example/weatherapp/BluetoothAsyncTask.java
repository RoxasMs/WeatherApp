package com.example.weatherapp;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class BluetoothAsyncTask extends AsyncTask<Void, String, Void> {
    BluetoothSocket socket;
    TextView lumField;
    TextView tempField;
    public BluetoothAsyncTask(BluetoothSocket socket, TextView lumField, TextView tempField) {
        this.socket = socket;
        this.lumField = lumField;
        this.tempField = tempField;

    }
    @Override
    protected Void doInBackground(Void... params) {
        try {
            InputStream inputStream = socket.getInputStream();
            while (!isCancelled()) {
                int bytesAvailable = inputStream.available();
                if (bytesAvailable > 0) {
                    byte[] packetBytes = new byte[bytesAvailable];
                    inputStream.read(packetBytes);
                    String data = new String(packetBytes, StandardCharsets.UTF_8);
                    if(data.contains("::"))publishProgress(data);
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        String data = values[0];
        Log.d("BluetoothData", data);

        String[] parts = data.split("::");
        lumField.setText(parts[0]);
        tempField.setText(parts[1]);

        lumField.setVisibility(View.VISIBLE);
        tempField.setVisibility(View.VISIBLE);

    }
}
