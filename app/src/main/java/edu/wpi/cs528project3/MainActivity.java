package edu.wpi.cs528project3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private StepCounter sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create step counter
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sc = new StepCounter(sensorManager);

    }
}