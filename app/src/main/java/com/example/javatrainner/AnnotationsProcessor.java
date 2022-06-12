package com.example.javatrainner;

import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class AnnotationsProcessor {

    protected String[] idxToAnnotationName = {"Nose", "LEye_i", "LEye", "REye_o", "REye_i",
            "REye", "REye_o", "LEar", "REar", "LLip", "RLip", "LShoulder", "RShoulder",
            "LElbow", "RElbow", "LWrist", "RWrist", "LHand_p", "RHand_p", "LHand_i", "RHand_i",
            "LHand_t", "RHand_t", "LHip", "RHip", "LKnee", "RKnee", "LAnkle", "RAnkle",
            "LHeel", "RHeel", "LTows", "RTows"};
    protected HashMap<String, ArrayList<Float>> currentState = new HashMap<String, ArrayList<Float>>();
    private static final String TAG = "AnnotationProcessor";
    protected ArrayList<HashMap>capturedRep = new ArrayList<HashMap>();


    void setNewState(NormalizedLandmarkList poseLandMarks){
        int landmarkIndex = 0;
        this.currentState = new HashMap<String, ArrayList<Float>>();
        for (LandmarkProto.NormalizedLandmark landmark: poseLandMarks.getLandmarkList()){
            if (landmark.getVisibility() < 0.8 && landmark.getPresence() < 0.8){
                ++landmarkIndex;
                continue;
            }
            String landmarkString = (String) Array.get(this.idxToAnnotationName, landmarkIndex);
            ArrayList<Float> landmarkCoordinates = new ArrayList<Float>(5);
            landmarkCoordinates.add(landmark.getX());
            landmarkCoordinates.add(landmark.getY());
            landmarkCoordinates.add(landmark.getZ());
            landmarkCoordinates.add(landmark.getPresence());
            landmarkCoordinates.add(landmark.getVisibility());
            this.currentState.put(landmarkString, landmarkCoordinates);
            ++landmarkIndex;
        }
    }


    String getDetectionCoordinates(String detectionName){
        StringBuilder output = new StringBuilder();
        ArrayList<Float> detecionCoord = this.currentState.get(detectionName);
        output.append("(").append(detecionCoord.get(0)).append(", ");
        output.append(detecionCoord.get(1)).append(", ").append(detecionCoord.get(2)).append(")");
        return output.toString();
    }


    String getPoseDetections(){
        StringBuilder output = new StringBuilder();
        for (String Detection: this.currentState.keySet()){
            output.append(Detection).append(", ");
        }
        return output.toString();
    }

    String getPoseDetectionsValues(){
        StringBuilder output = new StringBuilder();
        for (String detection: this.currentState.keySet()){
            output.append(detection).append(": ").append(this.getDetectionCoordinates(detection)).append("\n");
        }
        return output.toString();
    }

    Integer calculateAngle(String fNode, String mNode, String endNode){
        ArrayList<Float> fPoint = this.currentState.get(fNode);
        ArrayList<Float> mPoint = this.currentState.get(mNode);
        ArrayList<Float> lPoint = this.currentState.get(endNode);
        if (fPoint == null || mPoint == null || lPoint==null){
            return null;
        }
        double dotProduct = 0;
        double scalarSizeA = 0;
        double scalarSizeB = 0;
        for (int i = 0; i < 3 ; i++){
            float vecA = fPoint.get(i) - mPoint.get(i);
            float vecB = lPoint.get(i) - mPoint.get(i);
            dotProduct += vecA * vecB;
            scalarSizeA += vecA * vecA;
            scalarSizeB += vecB * vecB;
        }
        double angle_rad = Math.acos(dotProduct / (Math.sqrt(scalarSizeA) * Math.sqrt(scalarSizeB)));
        int angle = (int)Math.round(Math.toDegrees(angle_rad));
        return angle;
    }

    void addToCapturedRep(HashMap state){
        this.capturedRep.add(state);
    }

    void resetCapturedRep(){
        this.capturedRep = new ArrayList<HashMap>();
    }
}
