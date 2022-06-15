package com.example.javatrainner;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.mediapipe.components.PermissionHelper;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SquatProcessor squatProcessor;
    private VisionEngine visionEngine;
    private TechniqueCorrectUI techniqueCorrectUI;
    private Thread uiThread;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //runTechniqueCorrector();
        SquatClassifier squatClassifier = new SquatClassifier(this);
    }

    private void runTechniqueCorrector(){
        initTechniquePage();
        squatProcessor = new SquatProcessor(this);
        visionEngine = this.CreateVisionEngine();
        if (visionEngine != null){
            visionEngine.startVision();
        }
        this.uiThread = Thread.currentThread();
        this.handler = new Handler();
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true){
                    updateTechniqueCorrectorUi();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void updateTechniqueCorrectorUi(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                techniqueCorrectUI.setCounter(String.valueOf(squatProcessor.getRepCount()));
                techniqueCorrectUI.setSuccessPercentage(String.valueOf(squatProcessor.getSuccessPercentage()));
                techniqueCorrectUI.setUserFeedback(squatProcessor.getCurrentInstruction());
            }
        };
        this.runOnUi(runnable);
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
        setContentView(R.layout.technique_correct);
        TextView counter = findViewById(R.id.Counter);
        TextView successPer = findViewById(R.id.Success);
        TextView userFeedback = findViewById(R.id.CorrectionBox);
        ImageButton flipCamera = findViewById(R.id.flip_cam);
        flipCamera.setOnClickListener(v -> visionEngine.flipCamera());
        Button end_corrector = findViewById(R.id.end_corrector);
        end_corrector.setOnClickListener(v -> visionEngine.onPause());
        this.techniqueCorrectUI = new TechniqueCorrectUI(counter, successPer, userFeedback);
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