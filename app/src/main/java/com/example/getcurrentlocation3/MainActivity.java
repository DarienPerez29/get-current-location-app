package com.example.getcurrentlocation3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {

    TextView latitude, longitude, altitude, accuracy, speed;// Campos principales
    TextView updates, energy; // Campos de senores
    SwitchMaterial updates_switch, energy_switch; // Switches

    FusedLocationProviderClient fusedLocation; // API de Google para servicios de localización
    LocationRequest locationRequest; // Es un archivo de configuración para todos los ajustes de fusedLocation
    LocationCallback locationCallback;

    private static final int LOC_CODE = 99;

    // Variables para el delay
    Handler m_handler;
    Runnable m_handlerTask;
    final int delayInMin = 5; // Se cambia esta variable
    final int delay = delayInMin * 60 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitude = findViewById(R.id.latitude_res);
        longitude = findViewById(R.id.longitude_res);
        altitude = findViewById(R.id.altitude_res);
        accuracy = findViewById(R.id.accuracy_res);
        speed = findViewById(R.id.speed_res);
        updates = findViewById(R.id.updates_res);
        energy = findViewById(R.id.energy_res);
        updates_switch = findViewById(R.id.updates_switch);
        energy_switch = findViewById(R.id.energy_switch);

        // Declaramos las configuraciones del fusedLocation en locationRequest
        int LONG_INTERVAL = 3;
        int SHORT_INTERVAL = 1;
        locationRequest = LocationRequest.create()
                .setInterval(LONG_INTERVAL * 1000)
                .setFastestInterval(SHORT_INTERVAL * 1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Evento que se ejecuta cuando el intervalo de actualización coincide
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateValues(locationResult.getLastLocation());
            }
        };

        energy_switch.setOnClickListener(v -> {
            if (energy_switch.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                energy.setText("Priorizando la precisión (alto consumo)");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                energy.setText("Consumo y precisión balanceados");
            }
        });

        updates_switch.setOnClickListener(v -> {
            if (updates_switch.isChecked()) {
                startLocationUpdates();
                m_handler.postDelayed(m_handlerTask, 5000);
            } else {
                stopLocationUpdates();
                m_handler.removeCallbacks(m_handlerTask);
            }
        });

        m_handler = new Handler();
        m_handlerTask = () -> {
            stopLocationUpdates();
            updates_switch.setChecked(false);
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOC_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateGps();
        } else {
            Toast.makeText(this, "Necesita dar permisos para acceder a todas las funciones", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Actualizar GPS
    private void updateGps() {
        fusedLocation = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Si el usuario autoriza los permisos
            fusedLocation.getLastLocation().addOnSuccessListener(this, this::updateValues);
        } else {
            // Si no hay permisos, se solicitan nuevamente
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOC_CODE);
        }
    }

    // Actualizamos los campos con valores nuevos de una nueva localización
    private void updateValues(Location location) {
        latitude.setText(String.valueOf(location.getLatitude()));
        longitude.setText(String.valueOf(location.getLongitude()));
        accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()) altitude.setText(String.valueOf(location.getAltitude()));
        else altitude.setText("N/A");

        if (location.hasSpeed()) speed.setText(String.valueOf(location.getSpeed()));
        else speed.setText("N/A");
    }

    // Empezar localización continua
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        updateGps();
        updates.setText("Se esta realizando el seguimiento de la localización");
        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    // Terminar localización continua
    private void stopLocationUpdates() {
        updates.setText("No se esta realizando el seguimiento de la localización");

        latitude.setText("No hay localización");
        longitude.setText("No hay localización");
        altitude.setText("No hay localización");
        accuracy.setText("No hay localización");
        speed.setText("No hay localización");

        fusedLocation.removeLocationUpdates(locationCallback);
    }

}