package com.example.javatrainner;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.speech.tts.TextToSpeech;
import java.util.HashMap;
import java.util.Locale;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;


public class SquatClassifier extends TechniqueClassifier{
    int neighborCountMaxBin = 2;
    int neighborCountMinBin = 1;
    int neighborCountMaxMul = 8;
    int neighborCountMinMul = 3;
    private knnWrapper[] binArray = new knnWrapper[this.neighborCountMaxBin];
    private knnWrapper[] mulArray = new knnWrapper[this.neighborCountMaxMul];
    private knnWrapper binaryClassifier;
    private knnWrapper multiClassifier;
    private final String[] multiClasses = {"butFirst", "core", "headStraight", "liftingHeels", "noRep", "straightBack",
            "unEvenStance", "unEvenWeightBearing"};
    private final Map<String, String> instructions = new HashMap<String, String>() {{
        put("straightBack", "Try to keep your back straight during the motion");
        put("butFirst", "Try to keep your back straight during the motion");
        put("headStraight", "Try to keep your head straight during the motion");
        put("core", "Engage your core more");
        put("unEvenBearing", "Make sure you put equal weight on both legs");
        put("unEvenStance", "Be sure both feet are equally aligned");
        put("liftingHeels", "Keep your heels on the floor");
        put("noRep", "Try to keep your back straight during the motion");
    }};
    private  final String[] binClass = {"bad","good"};
    private String TAG = "SquatClassifier";
    private TextToSpeech speaker;

    SquatClassifier(Context context){
        super(context);
        this.speaker = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    (speaker).setLanguage(Locale.UK);
                }
            }});
        this.intiBinaryClassifier("binaryCFLDataNew.txt");
        this.intiMultiClassifier("MultiCFLDataNewKmean.txt");
    }

    public static int argmax(int[] a) {
        int re = Integer.MIN_VALUE;
        int arg = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > re) {
                re = a[i];
                arg = i;
            }
        }
        return arg;
    }


    String getClassification(double[][][] capturedMotion){
        double[][][] trimmedRep = this.trimRepSize(capturedMotion);
        if (trimmedRep == null) {
            this.speaker.speak("Slow down please", TextToSpeech.QUEUE_FLUSH, null);
            return "slowDown";
        }
        for (int i = 0; i < trimmedRep.length; i++){
            trimmedRep[i] = pe.embeddedLandmarks(trimmedRep[i]);
        }
        int[] binaryClass = new int[2];
        for (int i = this.neighborCountMinBin; i < this.neighborCountMaxBin; i++){
            int clazz = this.binArray[i].predict(trimmedRep);
            binaryClass[clazz]++;
            Log.i(this.TAG, "Binary classifier: " + i + " " + clazz);
        }
        int binClass = argmax(binaryClass);
        if (binClass == 1){
            this.speaker.speak("Good job", TextToSpeech.QUEUE_FLUSH, null);
            return this.binClass[binClass];
        } else {
            int[] multiClass = new int[8];
            for (int i = neighborCountMinMul; i < this.neighborCountMaxMul; i++) {
                int clazz = this.mulArray[i].predict(trimmedRep);
                multiClass[clazz]++;
                Log.i(this.TAG, "multi classifier: " + i + " " + clazz + this.multiClasses[clazz]);
            }
            int multiClazz = argmax(multiClass);
            this.speaker.speak(this.instructions.get(this.multiClasses[multiClazz]),
                    TextToSpeech.QUEUE_FLUSH, null);
            return this.multiClasses[multiClazz];
        }
    }

    static public int[] create1DIntArrayFromFile(String lines, int size) throws IOException {
        int[] output = new int[size];
        int i = 0;
        for (String s : lines.split(",")) {
            if (s.isEmpty()){continue;}
            output[i] = Integer.parseInt(s.replace(" ", ""));
            i++;
        }
        return output;
    }

    static public double[][] create2DDoubleMatrixFromFile(String data, int size) throws IOException {
        double[][] output = new double[size][660];
        String[] dataS = data.split(",");
        int k = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < 660; j++) {
                if (dataS[k].isEmpty()) {
                    k++;
                    j--;
                    continue;
                }
                output[i][j] = Double.parseDouble(dataS[k].replace(" ", ""));
                k++;
            }
        }
        return output;
    }

    private String editData(String data){
        data = data.replace("{", "");
        data = data.replace("}", "");
        data = data.replace(" ", "");
        return data;
    }

    private void intiBinaryClassifier(String fileName) {
        ArrayList<String> output = getDataPoints(fileName);
        String y = output.get(1);
        String x = output.get(0);

        x = editData(x);
        y = editData(y);
        int size = y.split(",").length;
        try {
            int[] yData = create1DIntArrayFromFile(y, size);
            double[][] xData = create2DDoubleMatrixFromFile(x, size);
            for (int i = this.neighborCountMinBin; i < this.neighborCountMaxBin; i++){
                this.binArray[i] = new knnWrapper(i + 1, 2, xData, yData);
            }
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
        int size = y.split(",").length;
        try {
            int[] yData = create1DIntArrayFromFile(y, size);
            double[][] xData = create2DDoubleMatrixFromFile(x, size);
            for (int i = this.neighborCountMinMul; i < this.neighborCountMaxMul; i++){
                this.mulArray[i] = new knnWrapper(i + 1, this.multiClasses.length, xData, yData);
            }
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