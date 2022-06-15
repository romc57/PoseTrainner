package com.example.javatrainner;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SquatClassifier extends TechniqueClassifier{
    private knnWrapper binaryClassifier;
    private knnWrapper multiClassifier;
    private final String[] multiClasses = {"butFirst", "core", "good", "headStraight",
            "liftingHeels", "noRep", "straightBack", "unEvenStance", "unEvenBearing"};
    private  final String[] binClass = {"bad","good"};
    private String TAG = "SquatClassifier";

    SquatClassifier(Context context){
        super(context);
        this.intiBinaryClassifier("binaryCLFFile.txt");
        this.intiMultiClassifier("MultiCLFFile.txt");
    }

    String getClassification(double[][][] capturedMotion){
        double[][][] trimmedRep = this.trimRepSize(capturedMotion);
        if (trimmedRep == null) {
            return "slowDown";
        }
        for (int i = 0; i < trimmedRep.length; i++){
            trimmedRep[i] = pe.embeddedLandmarks(trimmedRep[i]);
        }
        int bin = binaryClassifier.predict(trimmedRep);
        int multi = multiClassifier.predict(trimmedRep);
        Log.i(this.TAG, "MultiClass classifier: " + multi);
        Log.i(this.TAG, "Binary classifier: " + bin);
        if (bin == 0) {
            //int multi = multiClassifier.predict(trimmedRep);
            return this.multiClasses[multi];
        } else {
            return "good";
        }
    }

    static public int[] create1DIntArrayFromFile(String lines, int size) throws IOException {
//        String lines = Files.readString(path);
        int[] output = new int[size];
        int i = 0;
        for (String s : lines.split(" +")) {
            if (s.isEmpty()){continue;}
            output[i] = Integer.parseInt(s);
            i++;
        }
        return output;
    }

    static public double[][] create2DDoubleMatrixFromFile(String data, int size) throws IOException {
        double[][] output = new double[size][660];
        String[] dataS = data.split(" +");
        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < 660; j++) {
                if (dataS[k].isEmpty()) {continue;}
                output[i][j] = Double.parseDouble(dataS[k]);
                k++;
            }
        }
        return output;
    }

    private String editData(String data){
        data = data.replace("{", " ");
//            data = data.replace("\n\n", "");
        data = data.replace("}", " ");
        data = data.replace(",", " ");
        data = data.replace(";", "");
        return data;
    }

    private void intiBinaryClassifier(String fileName) {
        ArrayList<String> output = getDataPoints(fileName);
        String y = output.get(1);
        String x = output.get(0);

        x = editData(x);
        y = editData(y);
        int size = y.split(" +").length;
        try {
            int[] yData = create1DIntArrayFromFile(y, size);
            double[][] xData = create2DDoubleMatrixFromFile(x, size);
            this.binaryClassifier = new knnWrapper(3, 2, xData, yData);
        }
        catch (IOException e)  {
            e.printStackTrace();
        }
    }

    private void intiMultiClassifier(String fileName){
        ArrayList<String> output = getDataPoints(fileName);
        String y = output.get(1);
        String x = output.get(0);

        x = editData(x);
        y = editData(y);
        int size = y.split(" +").length;
        try {
            int[] yData = create1DIntArrayFromFile(y, size);
            double[][] xData = create2DDoubleMatrixFromFile(x, size);
            this.multiClassifier = new knnWrapper(3, 2, xData, yData);
        }
        catch (IOException e)  {
            e.printStackTrace();
        }
    }

    private ArrayList<String> getDataPoints(String fileName){
        ArrayList<String> output = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(this.context.getAssets().open(fileName), "UTF-8"));
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                output.add(mLine);
            }
            return output;
        } catch (IOException e) {
            Log.e(this.TAG, "Error, something went wrong while reading points for kNNN");
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    protected double[][][] trimRepSize(double[][][] rep){
        List<double[][]> newRep = new ArrayList<>();
        int repLength = rep.length;
        if (repLength == trimSize) {
            return rep;
        } else if (repLength < trimSize){
            return null;
        }
        double minS = -1.0;
        int minI = -1;
        for (int i = 0; i < repLength; i++){
            if (minS == -1.0 && minI == -1.0) {
                minS = rep[i][0][1];  // Nose Y position
                minI = i;
            } else if (rep[i][0][1] > minS) { // Y axis starts at the top of the frame
                minS = rep[i][0][1];  // Nose Y position
                minI = i;
            }
        }
        int midSize = (int) Math.ceil((float) this.trimSize / 2);
        if (midSize > minI){
            minI = midSize;
        }
        if (midSize > repLength - minI) {
            minI = repLength - midSize - 1;
        }
        int goingDown = minI;
        int goingUp = repLength - minI;
        double downInterval = goingDown / ((double) trimSize / 2);
        double upInterval = goingUp / ((double) trimSize / 2);
        double j = 0;
        while (newRep.size() < trimSize && Math.floor(j) < repLength) {
            int idx = (int) Math.floor(j);
            if (newRep.size() < midSize) {
                newRep.add(rep[idx]);
                j += downInterval;
            } else {
                newRep.add(rep[idx]);
                j += upInterval;
            }
        }
        Log.i(this.TAG, "Trimmed rep to size " + newRep.size());
        return (newRep.toArray(new double[3][newRep.get(0).length][newRep.size()])) ;
    }
}

