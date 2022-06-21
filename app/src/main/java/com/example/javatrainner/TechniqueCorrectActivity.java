package com.example.javatrainner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mediapipe.components.PermissionHelper;
import java.time.LocalTime;
import java.util.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimerTask;


public class TechniqueCorrectActivity extends AppCompatActivity {

    private TextView counter;
    private TextView currentDrill;
    private FrameLayout statusLine;
    private TextView shortComment;
    private Drawable goodRepDrawable;
    private Drawable badRepDrawable;
    private ImageView flipCamera;
    private TextView timeInTrain;
    private Button endActivity;
    private ImageView backToHome;
    private SquatProcessor squatProcessor;
    private VisionEngine visionEngine;
    private String TAG = "TechniqueCorrectActivity";
    private Timer autoUpdate;
    private Long startingTime = System.currentTimeMillis();

    @SuppressLint("UseCompatLoadingForDrawables")
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.train);
        counter = findViewById(R.id.repCount);
        shortComment = findViewById(R.id.shortComment);
        statusLine = findViewById(R.id.statusLine);
        goodRepDrawable = getDrawable(R.drawable.working_status_line_good);
        badRepDrawable = getDrawable(R.drawable.working_status_line_bad);
        currentDrill = findViewById(R.id.CurrentDrill);
        flipCamera = findViewById(R.id.flipCam);
        timeInTrain = findViewById(R.id.time_in_train);
        flipCamera.setOnClickListener(v -> this.flipCamera());
        endActivity = findViewById(R.id.end_train);
        endActivity.setOnClickListener(v -> this.backToHomeActivity());
        backToHome = findViewById(R.id.back_arrow_train);
        backToHome.setOnClickListener(v -> this.backToHomeActivity());
        squatProcessor = new SquatProcessor(this);
        visionEngine = CreateVisionEngine();
        if (visionEngine != null){
            visionEngine.startVision();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.visionEngine != null) {
            this.visionEngine.onResume();
        }
        this.autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateActivityFields();
                    }
                });
            }
        }, 0, 1000); // updates each 40 secs
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.visionEngine != null){
            this.visionEngine.onPause();
        }
        if (this.autoUpdate != null){
            this.autoUpdate.cancel();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private VisionEngine CreateVisionEngine() {
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

    public void updateActivityFields(){
        this.setCounter();
        this.setCurrentDrill();
        this.setUserFeedback();
        this.updateStatusBar();
        this.updateTimeInTrain();
    }

    public void setCounter(){
        int repCount = this.squatProcessor.getRepCount();
        this.counter.setText(String.valueOf(repCount));
        if (repCount == this.squatProcessor.sessionReps) {
            this.loadStatisticsActivity();
        }
    }

    public void setCurrentDrill(){
        this.currentDrill.setText(String.valueOf(this.squatProcessor.getCurrentDrill()));
    }

    public void setUserFeedback(){
        this.shortComment.setText(String.valueOf(this.squatProcessor.getCurrentInstruction()));
    }

    public void updateStatusBar(){
        if (this.squatProcessor.currentSquatStatus) {
            this.statusLine.setBackground(this.goodRepDrawable);
        } else {
            this.statusLine.setBackground(this.badRepDrawable);
        }
    }

    private float getTimeInSession(){
        Long currentTime = System.currentTimeMillis();
        float diff = (float) (currentTime - this.startingTime) / 1000;
        return diff;
    }

    private String getTimeInSessionString() {
        float diff = this.getTimeInSession();
        int min = (int) Math.floor(diff / 60);
        int sec = (int) (diff % 60);
        String minutes = (min < 10) ? "0" + min : String.valueOf(min);
        String seconds = (sec < 10) ? "0" + sec : String.valueOf(sec);
        return minutes + ":" + seconds;
    }

    public void updateTimeInTrain(){
        this.timeInTrain.setText(this.getTimeInSessionString());
    }

    private void flipCamera(){
        this.visionEngine.flipCamera();
    }

    private void backToHomeActivity(){
        Intent homeActivity = new Intent(this, MainActivity.class);
        startActivity(homeActivity);
    }

    private void loadStatisticsActivity(){
        Intent statActivity = new Intent(this, TrainSummeryActivity.class);
        Bundle b = new Bundle();
        b.putIntArray("intArray", this.assembleSummery());
        statActivity.putExtras(b);
        startActivity(statActivity);
        finish();
    }

    private int[] assembleSummery(){
        int[] output = {this.squatProcessor.getRepCount(), this.squatProcessor.getGoodRepCount(),
                (int) Math.floor(this.getTimeInSession())};
        return output;
    }
}
