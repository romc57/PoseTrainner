package com.example.javatrainner;

import android.util.Log;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.ArrayList;
import java.util.HashMap;

public class SquatProcessor extends AnnotationsProcessor{

    private static final String[] squatLandMarks = {"Nose", "LEye_i", "LEye", "REye_o", "REye_i",
            "REye", "REye_o", "LEar", "REar", "LLip", "RLip", "LShoulder", "RShoulder", "LHip",
            "RHip", "LKnee", "RKnee", "LAnkle", "RAnkle", "LHeel", "RHeel", "LTows", "RTows"};
    private static final int fullRepDegrees = 85;
    private final String TAG = "SquatProcessor";
    private int kneeDegreesDelta = 5;
    private static final String checkPositionLandmark = "LAnkle";
    private double checkPositionDelta = 0.1;
    private TechniqueClassifier techniqueClassifier = new TechniqueClassifier();
    private HashMap<String, ArrayList<Float>> currentSquatState = null;
    private Integer smallestLeftKneeAngle = null;
    private Integer biggestLeftKneeAngle = null;
    private Integer smallestRightKneeAngle = null;
    private Integer biggestRightKneeAngle = null;
    private String currentTag;
    private String currentInstruction = "Squat away!";
    private boolean goodDetection = false;
    private int goodCount = 0;
    private int repCount = 0;
    private ArrayList<Float> currentSquatPos = null;

    void setNewState(LandmarkProto.NormalizedLandmarkList poseLandMarks){
        super.setNewState(poseLandMarks);
        this.checkLandMarks();
        if (!this.goodDetection){return;}
        this.captureSquat();
    }

    private void setClassification(){
        this.goodCount++;
        this.currentInstruction = "Great Job!";
    }

    void captureSquat(){
        this.checkSquatPosition();
        Integer lAngle = this.calculateAngle("LHip", "LKnee", "LAnkle");
        Integer rAngle = this.calculateAngle("RHip", "RKnee", "RAnkle");
        Log.i(this.TAG, "Angles: " + String.valueOf(rAngle) + " " + String.valueOf(lAngle));
        Log.i(this.TAG, "RepCount: " + String.valueOf(this.repCount));
        if(lAngle == null || rAngle == null){return;}
        if (this.smallestLeftKneeAngle == null || this.biggestLeftKneeAngle == null){
            this.smallestLeftKneeAngle = this.biggestLeftKneeAngle = lAngle;
            this.smallestRightKneeAngle = this.biggestRightKneeAngle = rAngle;
        } if (lAngle < this.smallestLeftKneeAngle) {
            this.smallestLeftKneeAngle = lAngle;
        } if (rAngle < this.smallestRightKneeAngle) {
            this.smallestRightKneeAngle = rAngle;
        } if ((lAngle > (this.biggestLeftKneeAngle - this.kneeDegreesDelta)) ||
                (rAngle > (this.biggestRightKneeAngle - this.kneeDegreesDelta))){
            if (Math.abs(this.biggestLeftKneeAngle - this.smallestLeftKneeAngle) < fullRepDegrees &&
                    Math.abs(this.biggestRightKneeAngle - this.smallestRightKneeAngle) < fullRepDegrees){
                this.smallestRightKneeAngle = this.biggestRightKneeAngle = rAngle;
                this.smallestLeftKneeAngle = this.biggestLeftKneeAngle = lAngle;
            } else {
                this.repCount++;
                this.setClassification();
                this.addToCapturedRep(this.currentSquatState);
                this.endCapture();
                this.resetCapturedRep();
                this.smallestRightKneeAngle = this.biggestRightKneeAngle = null;
                this.smallestLeftKneeAngle = this.biggestLeftKneeAngle = null;
            }
            this.resetCapturedRep();
        } else {
            this.addToCapturedRep(this.currentSquatState);
        }
    }

    void checkLandMarks(){
        HashMap<String, ArrayList<Float>> squatState = new HashMap<>();
        for (String squatLandmark: squatLandMarks){
            if (!this.currentState.containsKey(squatLandmark)){
                this.goodDetection = false;
                return;
            } else {
                squatState.put(squatLandmark, this.currentState.get(squatLandmark));
            }
        }
        this.currentSquatState = squatState;
        this.goodDetection = true;
    }

    void checkSquatPosition(){
        ArrayList<Float> refPoint = this.currentSquatState.get(checkPositionLandmark);
        if (this.currentSquatPos == null){
            this.currentSquatPos = refPoint;
            this.smallestLeftKneeAngle = this.biggestLeftKneeAngle = null;
            this.resetCapturedRep();
        } else {
            for (int i = 0; i < 2; i++){
                if (Math.abs(this.currentSquatPos.get(i) - refPoint.get(i)) > checkPositionDelta){
                    this.currentSquatPos = refPoint;
                    this.resetCapturedRep();
                    this.smallestLeftKneeAngle = this.biggestLeftKneeAngle = null;
                    this.smallestRightKneeAngle = this.biggestRightKneeAngle = null;
                }
            }
        }
    }

    int getRepCount(){
        return this.repCount;
    }

    float getSuccessPercentage() {
        if (this.repCount != 0){
            return (float) this.goodCount / this.repCount;
        } else {
            return 0;
        }
    }

    int getGoodRepCount() {return this.goodCount;}

    String getCurrentInstruction() {return this.currentInstruction;}

    void setKneeDegreesDelta(int newVal){
        this.kneeDegreesDelta = newVal;
    }

    void setCheckPositionDelta(double newVal){
        this.checkPositionDelta = newVal;
    }
}
