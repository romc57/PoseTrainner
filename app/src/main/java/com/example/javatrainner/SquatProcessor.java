package com.example.javatrainner;

import com.google.mediapipe.formats.proto.LandmarkProto;

import java.util.ArrayList;
import java.util.HashMap;

public class SquatProcessor extends AnnotationsProcessor{

    private static final String[] squatLandMarks = {"Nose", "LEye_i", "LEye", "REye_o", "REye_i",
            "REye", "REye_o", "LEar", "REar", "LLip", "RLip", "LShoulder", "RShoulder", "LHip",
            "RHip", "LKnee", "RKnee", "LAnkle", "RAnkle", "LHeel", "RHeel", "LTows", "RTows"};
    private static final int fullRepDegrees = 90;
    private int kneeDegreesDelta = 20;
    private static final String checkPositionLandmark = "LAnkle";
    private double checkPositionDelta = 0.15;

    private HashMap<String, ArrayList<Float>> currentSquatState = null;
    private Integer smallestKneeAngle = null;
    private Integer biggestKneeAngle = null;
    private boolean goodDetection = false;
    private int repCount = 0;
    private ArrayList<Float> currentSquatPos = null;

    void setNewState(LandmarkProto.NormalizedLandmarkList poseLandMarks){
        super.setNewState(poseLandMarks);
        this.checkLandMarks();
        if (!this.goodDetection){return;}
        this.captureSquat();
    }

    void captureSquat(){
        this.checkSquatPosition();
        Integer lAngle = this.calculateAngle("LHip", "LKnee", "LAnkle");
        Integer rAngle = this.calculateAngle("RHip", "RKnee", "RAnkle");
        if(lAngle == null || rAngle == null){return;}
        int averageAngle = (lAngle + rAngle) / 2;
        if (this.smallestKneeAngle == null || this.biggestKneeAngle == null){
            this.smallestKneeAngle = this.biggestKneeAngle = averageAngle;
            this.addToCapturedRep(this.currentSquatState);
        }  else if (averageAngle < this.smallestKneeAngle){
            this.smallestKneeAngle = averageAngle;
            this.addToCapturedRep(this.currentSquatState);
        } else if (this.smallestKneeAngle < averageAngle &&
                averageAngle < this.biggestKneeAngle - this.kneeDegreesDelta){
            this.addToCapturedRep(this.currentSquatState);
        } else if (averageAngle > this.biggestKneeAngle - this.kneeDegreesDelta){
            if (this.biggestKneeAngle - this.smallestKneeAngle < fullRepDegrees){
                this.resetCapturedRep();
                this.biggestKneeAngle = this.smallestKneeAngle = averageAngle;
            } else {
                this.repCount++;
                this.biggestKneeAngle = this.smallestKneeAngle = null;
                this.resetCapturedRep(); // Here capture the motion
            }
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
            this.smallestKneeAngle = this.biggestKneeAngle = null;
            this.resetCapturedRep();
        } else {
            for (int i = 0; i < 2; i++){
                if (Math.abs(this.currentSquatPos.get(i) - refPoint.get(i)) > checkPositionDelta){
                    this.currentSquatPos = refPoint;
                    this.resetCapturedRep();
                    this.smallestKneeAngle = this.biggestKneeAngle = null;
                }
            }
        }
    }

    int getRepCount(){
        return this.repCount;
    }

    ArrayList<Integer> getKneeAngleRange(){
        if (this.biggestKneeAngle == null || this.smallestKneeAngle == null){return null;}
        ArrayList<Integer> output = new ArrayList<>();
        output.add(this.smallestKneeAngle);
        output.add(this.biggestKneeAngle);
        return output;
    }

    void setKneeDegreesDelta(int newVal){
        this.kneeDegreesDelta = newVal;
    }

    void setCheckPositionDelta(double newVal){
        this.checkPositionDelta = newVal;
    }
}
