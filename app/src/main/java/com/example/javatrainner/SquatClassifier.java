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
        for (int i = 0; i < trimmedRep.length; i++){
            trimmedRep[i] = pe.embeddedLandmarks(trimmedRep[i]);
        }
        int bin = binaryClassifier.predict(trimmedRep);
        if (bin == 0) {
            int multi = multiClassifier.predict(trimmedRep);
            return this.multiClasses[multi];
        } else {
            return null;
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
}

