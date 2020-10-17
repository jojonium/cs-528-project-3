package edu.wpi.cs528project3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.widget.TextView;

import java.util.ArrayList;

public class StepCounter implements SensorEventListener {
    private static int SMOOTHING_WINDOW_SIZE = 10;

    private static SensorManager mSensorManager;
    private static TextView stepsTextView;
    private static String stepsTextPat;

    public static Sensor mSensorCount, mSensorAcc;
    private float mRawAccelValues[] = new float[3];

    // smoothing accelerometer signal variables
    private float mAccelValueHistory[][] = new float[3][SMOOTHING_WINDOW_SIZE];
    private float mRunningAccelTotal[] = new float[3];
    private float mCurAccelAvg[] = new float[3];
    private int mCurReadIndex = 0;

    public static int mStepCounter = 0;

    private ArrayList<Double> accelList;

    private double curMag = 0;
    private double avgMag = 0;

    //peak detection variables
    private double stepThreshold = 1.0;
    private double noiseThreshold = 2;
    private int windowSize = 10;

    public StepCounter(SensorManager sensorManager, TextView textView, String textPattern){
        mSensorManager = sensorManager;
        stepsTextView = textView;
        stepsTextPat = textPattern;
        mSensorCount = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorCount, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI);

        accelList = new ArrayList<>();

        stepsTextView.setText(String.format(stepsTextPat, (int) mStepCounter));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                // Extract 3D vector https://web.cs.wpi.edu/~emmanuel/courses/cs528/F20/slides/papers/deepak_ganesan_pedometer.pdf
                mRawAccelValues[0] = event.values[0];
                mRawAccelValues[1] = event.values[1];
                mRawAccelValues[2] = event.values[2];

                curMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));

                 // Smoothing
                for (int i = 0; i < 3; i++) {
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex];
                    mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i];
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex];
                    mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
                }
                System.out.println(mRunningAccelTotal);
                mCurReadIndex++;
                if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                    mCurReadIndex = 0;
                }

                avgMag = Math.sqrt(Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2));

                accelList.add(new Double(curMag - avgMag)); // Apply smoothing and add to list
        }

        if (accelList.size() >= windowSize) {
            peakDetection();
            stepsTextView.setText(String.format(stepsTextPat, (int) mStepCounter));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void peakDetection() {
        double forwardSlope = 0;
        double downwardSlope = 0;

        for (int i = 1; i < accelList.size() - 1; i++) {

            forwardSlope = accelList.get(i + 1) - accelList.get(i);
            downwardSlope = accelList.get(i) - accelList.get(i - 1);

            if (forwardSlope < 0 && downwardSlope > 0 && accelList.get(i) > stepThreshold && accelList.get(i) < noiseThreshold) {
                mStepCounter += 1;
            }
        }
        accelList = new ArrayList<>();
    }

}
