package edu.wpi.cs528project3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.widget.TextView;


import androidx.annotation.RequiresApi;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StepCounter implements SensorEventListener {
    private static int SMOOTHING_WINDOW_SIZE = 20;

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

    public static float mStepCounter = 0;
    public static float mStepCounterAndroid = 0;
    public static float mInitialStepCount = 0;

    private double mGraph1LastXValue = 0d;
    private double mGraph2LastXValue = 0d;

    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;

    private double lastMag = 0d;
    private double avgMag = 0d;
    private double netMag = 0d;

    //peak detection variables
    private double lastXPoint = 1d;
    double stepThreshold = 1.0d;
    double noiseThreshold = 2d;
    private int windowSize = 10;

    public StepCounter(SensorManager sensorManager, TextView textView, String textPattern){
        mSensorManager = sensorManager;
        stepsTextView = textView;
        stepsTextPat = textPattern;
        mSensorCount = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensorCount, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI);

        mSeries2 = new LineGraphSeries<>();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSensorChanged(SensorEvent e) {
        switch (e.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                mRawAccelValues[0] = e.values[0];
                mRawAccelValues[1] = e.values[1];
                mRawAccelValues[2] = e.values[2];

                lastMag = Math.sqrt(Math.pow(mRawAccelValues[0], 2) + Math.pow(mRawAccelValues[1], 2) + Math.pow(mRawAccelValues[2], 2));

                //Source: https://github.com/jonfroehlich/CSE590Sp2018
                for (int i = 0; i < 3; i++) {
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex];
                    mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i];
                    mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex];
                    mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE;
                }
                mCurReadIndex++;
                if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                    mCurReadIndex = 0;
                }

                avgMag = Math.sqrt(Math.pow(mCurAccelAvg[0], 2) + Math.pow(mCurAccelAvg[1], 2) + Math.pow(mCurAccelAvg[2], 2));

                netMag = lastMag - avgMag; //removes gravity effect

//                //update graph data points
//                mGraph1LastXValue += 1d;
//                mSeries1.appendData(new DataPoint(mGraph1LastXValue, lastMag), true, 60);

                mGraph2LastXValue += 1d;
                mSeries2.appendData(new DataPoint(mGraph2LastXValue, netMag), true, 60);
        }

        peakDetection();

        stepsTextView.setText(String.format(stepsTextPat, (int) mStepCounter));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void peakDetection() {
        double highestValX = mSeries2.getHighestValueX();

        if (highestValX - lastXPoint < windowSize) {
            return;
        }

        Iterator<DataPoint> valuesInWindow = mSeries2.getValues(lastXPoint, highestValX);

        lastXPoint = highestValX;

        double forwardSlope = 0d;
        double downwardSlope = 0d;

        List<DataPoint> dataPointList = new ArrayList<DataPoint>();
        valuesInWindow.forEachRemaining(dataPointList::add); //This requires API 24 or higher

        for (int i = 0; i < dataPointList.size(); i++) {
            if (i == 0) continue;
            else if (i < dataPointList.size() - 1) {
                forwardSlope = dataPointList.get(i + 1).getY() - dataPointList.get(i).getY();
                downwardSlope = dataPointList.get(i).getY() - dataPointList.get(i - 1).getY();

                if (forwardSlope < 0 && downwardSlope > 0 && dataPointList.get(i).getY() > stepThreshold && dataPointList.get(i).getY() < noiseThreshold) {
                    mStepCounter += 1;
                }
            }
        }
    }

}
