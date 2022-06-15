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


}
