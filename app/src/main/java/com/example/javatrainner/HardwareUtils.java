package com.example.javatrainner;

import android.hardware.SensorManager;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.getSystemServiceName;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


import java.util.List;

public class HardwareUtils {

    final SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    private Object getSystemService(String sensorService) {
        return null;
    }

    private void initAccelerometer(){
        SensorEventListener sel = new SensorEventListener(){
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
            public void onSensorChanged(SensorEvent event) {
                float[] values = event.values;
            }
        };
        List<Sensor> list;
        list = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(list.size()>0) {
            sm.registerListener(sel, list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}
