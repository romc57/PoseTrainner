package com.example.javatrainner;

import android.hardware.Sensor;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import android.util.Size;
import android.util.SizeF;

import java.util.ArrayList;
import java.util.List;

public class ThreeDimConvertor {

    private Float x_axis = null;
    private Float y_axis = null;
    private Float z_axis = null;
    private Float horizontalAngle = null;
    private Float verticalAngle = null;
    public Size viewSize = null;
    public Size displaySize = null;
    private  Integer lensInfoFocusDistanceCalibration = null;
    private Float lensInfoHyperFocalDistance = null;
    private Float lensInfoMinimumFocusDistance = null;
    private Integer lensInfoFocusDistanceCalibrationCalibrated = null;
    public CameraManager cameraManager = null;

    void setPhonePosition(float[] sensorValues){
        this.x_axis = sensorValues[0];
        this.y_axis = sensorValues[1];
        this.z_axis = sensorValues[2];
    }



    List<Float> getCameraAbsAng(){
        List<Float> output = new ArrayList<Float>(3);
        output.add(this.x_axis * 9);
        output.add(this.y_axis * 9);
        output.add(this.z_axis * 9);
        return output;
    }

    List<Float> getCameraAng(){
        List<Float> output = new ArrayList<Float>(2);
        output.add((float) this.horizontalAngle);
        output.add((float) this.verticalAngle);
        return output;
    }

    void setCameraCharacteristics(){
        if(this.cameraManager == null){return;}
        try {
            String camId = null;
            for (final String cameraId: this.cameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = this.cameraManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT){
                    camId = cameraId;
                    break;
                }
            }
            CameraCharacteristics characteristics = this.cameraManager.getCameraCharacteristics(camId);
            float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            SizeF size = (SizeF) characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            float w = size.getWidth();
            float h = size.getHeight();
            this.lensInfoHyperFocalDistance = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
            this.horizontalAngle = (float) (2*Math.atan(w/(maxFocus[0]*2)));
            this.verticalAngle = (float) (2*Math.atan(h/(maxFocus[0]*2)));
            this.lensInfoFocusDistanceCalibration = characteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
            this.lensInfoMinimumFocusDistance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        } catch (CameraAccessException e){
            return;
        }
    }
}
