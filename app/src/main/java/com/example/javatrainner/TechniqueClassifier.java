package com.example.javatrainner;
import android.content.Context;
import java.util.List;
import java.util.ArrayList;

public class TechniqueClassifier {

    protected int trimSize = 20;
    protected final PoseEmbedder pe = new PoseEmbedder(2.5);
    protected Context context;

    TechniqueClassifier(Context context){
        this.context = context;
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
                minS = rep[i][5][1];
                minI = i;
            } else if (rep[i][5][1] < minS) {
                minS = rep[i][5][1];
                minI = i;
            }
        }
        int midSize = (int) Math.ceil((float) repLength / 2);
        if (midSize > minI){
            minI = midSize;
        }
        if (minI >= repLength - midSize) {
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
        return (newRep.toArray(new double[3][newRep.get(0).length][newRep.size()])) ;
    }
}
