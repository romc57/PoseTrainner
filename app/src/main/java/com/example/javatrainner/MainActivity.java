package com.example.javatrainner;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.mediapipe.components.PermissionHelper;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        bindHomeButtons();
    }

    private void bindHomeButtons() {
        ImageView squatBtn = findViewById(R.id.sqautWorkOut);
        squatBtn.setOnClickListener(v -> this.startTechniqueActivity());
    }

    private void startTechniqueActivity(){
        Intent techniqueActivity = new Intent(this, TechniqueCorrectActivity.class);
        startActivity(techniqueActivity);
    }
}