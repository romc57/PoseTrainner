package com.example.javatrainner;
import android.os.Handler;
import android.text.format.Time;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.mediapipe.components.PermissionHelper;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SquatProcessor squatProcessor;
    private VisionEngine visionEngine;
    private TechniqueCorrectUI techniqueCorrectUI;
    private Thread uiThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.uiThread = Thread.currentThread();
        this.handler = new Handler();
        super.onCreate(savedInstanceState);
        runLandingPage();
    }

    private void runOnUi(Runnable runnable){
        if (Thread.currentThread() == uiThread){
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initTechniquePage(){
        TextView counter = findViewById(R.id.repCount);
        TextView currentDrill = findViewById(R.id.CurrentDrill);
        ImageView flipCamera = findViewById(R.id.flipCam);
        FrameLayout statusLine = findViewById(R.id.statusLine);
        flipCamera.setOnClickListener(v -> visionEngine.flipCamera());
        Button end_corrector = findViewById(R.id.end_train);
        end_corrector.setOnClickListener(v -> this.runTrainSummery());
        ImageView backToHome = findViewById(R.id.back_arrow_train);
        backToHome.setOnClickListener(v -> this.backToHome());
        this.techniqueCorrectUI = new TechniqueCorrectUI(counter, currentDrill, statusLine);
    }

    private void runTechniqueCorrector(){
        squatProcessor = new SquatProcessor(this);
        visionEngine = this.CreateVisionEngine();
        initTechniquePage();
        if (visionEngine != null){
            visionEngine.startVision();
        }
        this.runLandingPage();
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            Log.i(this.TAG, "Done sleeping");
        }
        setContentView(R.layout.train);
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true){
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            techniqueCorrectUI.setCounter(String.valueOf(squatProcessor.getRepCount()));
                            techniqueCorrectUI.setCurrentDrill(String.valueOf(squatProcessor.getCurrentDrill()));
                        }
                    };
                    runOnUi(runnable);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e){
                        break;
                    }
                }
            }
        }.start();
    }

    private void runLandingPage(){
        setContentView(R.layout.home);
        ImageView squatBtn = findViewById(R.id.sqautWorkOut);
        squatBtn.setOnClickListener(v -> this.runTechniqueCorrector());
    }

    private void runLoadingPage(){
        setContentView(R.layout.loading);
        LoadingScreenActivity loader = new LoadingScreenActivity(
                findViewById(R.id.LTCorner),
                findViewById(R.id.RTCorner),
                findViewById(R.id.RBCorner),
                findViewById(R.id.LBCorner),
                findViewById(R.id.skelaton)
        );
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true){
                    Runnable runner = new Runnable() {
                        @Override
                        public void run() {
                            loader.iterateCornerColors();
                        }
                    };
                    runOnUi(runner);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e){
                        break;
                    }
                }
            }
        }.start();
    }

    private void runTrainSummery(){
        this.onPause();
        this.visionEngine = null;
        setContentView(R.layout.train_summery);
        Button homeBtn = findViewById(R.id.statistics_home_btn);
        homeBtn.setOnClickListener(v -> this.backToHome());
        ImageView backBtn = findViewById(R.id.back_arrow);
        backBtn.setOnClickListener(v -> this.runTechniqueCorrector());
        ImageView closeBtn = findViewById(R.id.close_window);
        closeBtn.setOnClickListener(v -> this.runTechniqueCorrector());
        TrainSummeryActivity trainSummeryActivity = new TrainSummeryActivity();
        int[] controlledElementsIds = trainSummeryActivity.getControlledElements();
        View[] controlledElementsView = new View[controlledElementsIds.length];
        for (int i = 0; i < controlledElementsIds.length; i++){
            controlledElementsView[i] = findViewById(controlledElementsIds[i]);
        }
        trainSummeryActivity.setControlledElements(controlledElementsView);
    }

    private void backToHome(){
        this.onPause();
        this.visionEngine = null;
        this.runLandingPage();
    }

    private void backToPrev(){
        Log.i(TAG, "Going bach to prev");
    }

    private void closeWindow(){
        Log.i(TAG, "Closing window");
    }


    private VisionEngine CreateVisionEngine(){
        SurfaceView previewDisplayView = new SurfaceView(this);
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.PosePresent);
        viewGroup.addView(previewDisplayView);
        try {
            ApplicationInfo applicationInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            VisionEngine visionEngine = new VisionEngine(this.squatProcessor, previewDisplayView,
                    applicationInfo, this);
            return visionEngine;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.visionEngine != null) {
            this.visionEngine.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.visionEngine != null){
            this.visionEngine.onPause();
        }
    }
}