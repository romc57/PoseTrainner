package com.example.javatrainner;

import android.content.Context;
import android.util.Log;
import com.google.mediapipe.formats.proto.LandmarkProto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class SquatProcessor extends AnnotationsProcessor{

    private static final String[] squatLandMarks = {"Nose", "LEye_i", "LEye", "REye_o", "REye_i",
            "REye", "REye_o", "LEar", "REar", "LLip", "RLip", "LShoulder", "RShoulder", "LHip",
            "RHip", "LKnee", "RKnee", "LAnkle", "RAnkle", "LHeel", "RHeel", "LTows", "RTows"};
    private static final int fullRepDegrees = 85;
    private final String TAG = "SquatProcessor";
    private int kneeDegreesDelta = 5;
    private static final String checkPositionLandmark = "LAnkle";
    private double checkPositionDelta = 0.1;
    private int squatCalibrateFrames = 7;
    private SquatClassifier squatClassifier;
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
    private final String[] goodFeedback = {"Very good!", "Doing good, keep going!",
            "Are you a professional?", "Great rep!"};
    private final String[] badFeedback = {"You can do better.\nFocus!",
            "Not good enough!", "Try to listen to the instructions", "Everyone sucked once,\ntry again!"};
    private Context context;

    SquatProcessor(Context context){
        this.context = context;
        this.squatClassifier = new SquatClassifier(this.context);
    }

    void setNewState(LandmarkProto.NormalizedLandmarkList poseLandMarks){
        super.setNewState(poseLandMarks);
        this.checkLandMarks();
        if (!this.goodDetection){return;}
        this.captureSquat();
    }

    private void setClassification(){
        double[][][] arrayRep = this.getRepList();
        int frameCount = arrayRep.length;
        int landmarkCount = arrayRep[0].length;
        int dimSize = arrayRep[0][0].length;
        Log.i(this.TAG, String.format("Starting to process a rep with %2d frames, %2d landmarks, " +
                "%2d dimensions", frameCount, landmarkCount, dimSize));
        this.currentTag = squatClassifier.getClassification(arrayRep);
        if (this.currentTag.isEmpty()) {
            Log.e(this.TAG, "Something went wrong during classification");
            return;
        } else if (this.currentTag == "slowDown"){
            this.currentInstruction = "Didn't catch that one.\nPlease slow down.";
            return;
        }
        this.repCount++;
        Log.i(this.TAG, String.format("Rep got tagged as %s", this.currentTag));
        if (Objects.equals(this.currentTag, "good")){
            this.currentInstruction = this.getRandomFeedback(this.goodFeedback);
            this.goodCount ++;
        } else {
            //this.currentInstruction = this.getRandomFeedback(this.badFeedback);
            this.currentInstruction = this.currentTag;
        }
    }

    void captureSquat(){
        this.checkSquatPosition();
        Integer lAngle = this.calculateAngle("LHip", "LKnee", "LAnkle");
        Integer rAngle = this.calculateAngle("RHip", "RKnee", "RAnkle");
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
                this.addToCapturedRep(this.currentSquatState);
                this.setClassification();
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
            this.squatCalibrateFrames = 10;
        } else {
            for (int i = 0; i < 2; i++){
                if (Math.abs(this.currentSquatPos.get(i) - refPoint.get(i)) > checkPositionDelta){
                    this.currentSquatPos = refPoint;
                    this.resetCapturedRep();
                    this.smallestLeftKneeAngle = this.biggestLeftKneeAngle = null;
                    this.smallestRightKneeAngle = this.biggestRightKneeAngle = null;
                    this.squatCalibrateFrames = 10;
                    return;
                }
            }
            if (this.squatCalibrateFrames != 0){
                this.squatCalibrateFrames--;
                this.resetCapturedRep();
                this.smallestLeftKneeAngle = this.biggestLeftKneeAngle = null;
                this.smallestRightKneeAngle = this.biggestRightKneeAngle = null;
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

    private String getRandomFeedback(String[] feedbackOptions){
        int randomInt = ThreadLocalRandom.current().nextInt(0, feedbackOptions.length + 1);
        return feedbackOptions[randomInt];
    }

    double[][][] getRepList(){
        List<double[][]> output = new ArrayList<>();
        int repLen = this.capturedRep.size();
        for (int i = 0; i < repLen; i++){
            List<double[]> tempOutput = new ArrayList<>();
            HashMap<String, ArrayList<Float>> currState = capturedRep.get(i);
            for (String squatLandMark: squatLandMarks){
                ArrayList<Float> landmark = currState.get(squatLandMark);
                double[] coordinates = {(double) landmark.get(0), (double) landmark.get(1),
                        (double) landmark.get(2)};
                tempOutput.add(coordinates);
            }
            double[][] tempArray = new double[3][tempOutput.size()];
            tempArray = tempOutput.toArray(tempArray);
            output.add(tempArray);
        }
        double[][][] tempArray = new double[3][output.get(0).length][output.size()];
        tempArray = output.toArray(tempArray);
        return tempArray;
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
