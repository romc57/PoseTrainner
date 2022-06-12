package com.example.javatrainner;

import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final SquatProcessor squatProcessor = new SquatProcessor();
    SurfaceView previewDisplayView;
    ViewGroup viewGroup;
    private ApplicationInfo applicationInfo;
    private VisionEngine visionEngine = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.technique_correct);
        previewDisplayView = new SurfaceView(this);
        previewDisplayView.setVisibility(View.GONE);
        viewGroup = findViewById(R.id.PosePresent);
        viewGroup.addView(previewDisplayView);
        try {
            applicationInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }
        CameraManager systemCameraService = (CameraManager) getSystemService(CAMERA_SERVICE);
        this.visionEngine = new VisionEngine(this.squatProcessor, previewDisplayView, applicationInfo,
                this, systemCameraService);
        this.visionEngine.startVision();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.visionEngine.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.visionEngine.onPause();
    }

}