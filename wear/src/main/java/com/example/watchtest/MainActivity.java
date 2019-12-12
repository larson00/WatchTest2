package com.example.watchtest;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements DataClient.OnDataChangedListener {
    private static final String COUNT_KEY = "com.example.count";
    private DataClient mDataClient;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String LOG_TAG = "Wearable";

    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private Sensor mAvailableSensor;
    private Integer currentValue=0;
    private Integer heartRate;
    private String retrieveMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        // SensorManager
        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        // initialise API client for sending data to phone here
        mDataClient = Wearable.getDataClient(this);
    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");
        if (mHeartRateSensor == null) {
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sensor1 : sensors) {
                Log.i(TAG, sensor1.getName() + ": " + mAvailableSensor.getType());
            }
        }
        mDataClient = Wearable.getDataClient(this);
    }
    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener((SensorEventListener)
                this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }
    private void stopMeasure() {
        mSensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mDataClient = Wearable.getDataClient(this);
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
//        for (DataEvent dataEvent : dataEvents) {
//            if (dataEvent.getType() == DataEvent.TYPE_DELETED) {
//                Log.d(TAG, "DataItem deleted: " + dataEvent.getDataItem().getUri());
//            } else if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
//                Log.d(TAG, "DataItem changed: " + dataEvent.getDataItem().getUri());
//            }
//        }
        for(DataEvent dataEvent : dataEvents){
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = dataEvent.getDataItem();
                if(item.getUri().getPath().compareTo("/heartrate") == 0){
                    DataMap dataMap =
                            DataMapItem.fromDataItem(item).getDataMap();
                    heartRate = dataMap.getInt("heart-rate");
                   // retrieveMessage(Integer.toString(heartRate));
                } } }
    }



    // Create a data map and put data in it
    public void sendData(ArrayList<String> data) {
        Log.d(TAG, "sending data");

        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/count"); // create data map
        putDataMapReq.getDataMap().putStringArrayList(COUNT_KEY, data); // put data in map

        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();

        Task<DataItem> putDataTask = mDataClient.putDataItem(putDataReq); // ERROR COMES FROM THIS LINE
        putDataTask.addOnSuccessListener(
                new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Log.d(TAG, "Sending text was successful: " + dataItem);
                    }
                });


    }
}
