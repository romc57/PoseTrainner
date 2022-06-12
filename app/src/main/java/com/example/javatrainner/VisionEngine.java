package com.example.javatrainner;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;


public class VisionEngine {

    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";
    private static final boolean FLIP_FRAMES_VERTICALLY = true;
    private static final int NUM_BUFFERS = 2;
    private static final String TAG = "VisionEngine";
    static {
        // Load all native libraries needed by the app.
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");

    }
    protected FrameProcessor processor;
    protected CameraXPreviewHelper cameraHelper;
    private SurfaceTexture previewFrameTexture;
    private SurfaceView previewDisplayView = null;
    private EglManager eglManager;
    private ExternalTextureConverter converter;
    private ApplicationInfo applicationInfo = null;
    private AnnotationsProcessor annotationsProcessor = null;
    private Context context = null;
    private CameraHelper.CameraFacing cameraFacing = CameraHelper.CameraFacing.FRONT;

    VisionEngine(AnnotationsProcessor annotationsProcessor, SurfaceView surfaceView,
                 ApplicationInfo applicationInfo, Context main){
        this.annotationsProcessor = annotationsProcessor;
        this.previewDisplayView = surfaceView;
        this.applicationInfo = applicationInfo;
        this.context = main;
    }

    void startVision(){
        setupPreviewDisplayView();
        eglManager = new EglManager(null);
        processor =
                new FrameProcessor(
                        this.context,
                        eglManager.getNativeContext(),
                        applicationInfo.metaData.getString("binaryGraphName"),
                        applicationInfo.metaData.getString("inputVideoStreamName"),
                        applicationInfo.metaData.getString("outputVideoStreamName"));
        processor
                .getVideoSurfaceOutput()
                .setFlipY(
                        applicationInfo.metaData.getBoolean("flipFramesVertically", FLIP_FRAMES_VERTICALLY));
        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
                    try {
                        byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                        LandmarkProto.NormalizedLandmarkList poseLandmarks = LandmarkProto.NormalizedLandmarkList.parseFrom(landmarksRaw);
                        annotationsProcessor.setNewState(poseLandmarks);
                    } catch (InvalidProtocolBufferException exception) {
                        Log.e(TAG, "Failed to get proto.", exception);
                    }
                });
        PermissionHelper.checkAndRequestCameraPermissions((Activity) this.context);
    }

    public void flipCamera(){
        if (this.cameraFacing == CameraHelper.CameraFacing.FRONT){
            this.cameraFacing = CameraHelper.CameraFacing.BACK;
        } else {
            this.cameraFacing = CameraHelper.CameraFacing.FRONT;
        }
        this.onPause();
        this.onResume();
    }

    protected void onResume() {
        converter =
                new ExternalTextureConverter(
                        eglManager.getContext(),
                        applicationInfo.metaData.getInt("converterNumBuffers", NUM_BUFFERS));
        converter.setFlipY(
                applicationInfo.metaData.getBoolean("flipFramesVertically", FLIP_FRAMES_VERTICALLY));
        converter.setConsumer(processor);
        if (PermissionHelper.cameraPermissionsGranted((Activity) this.context)) {
            try{
                startCamera();
            } catch (CameraAccessException e){
                Log.e(TAG, "Hi");
            }

        }
    }

    protected void onPause() {
        converter.close();
        previewDisplayView.setVisibility(View.GONE);
    }


    protected void onCameraStarted(SurfaceTexture surfaceTexture) {
        previewFrameTexture = surfaceTexture;
        previewDisplayView.setVisibility(View.VISIBLE);
    }

    protected Size cameraTargetResolution() {
        return null; // No preference and let the camera (helper) decide.
    }

    public void startCamera() throws CameraAccessException {
        cameraHelper = new CameraXPreviewHelper();
        previewFrameTexture = converter.getSurfaceTexture();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    onCameraStarted(surfaceTexture);
                });
        cameraHelper.startCamera(
                (Activity) this.context, this.cameraFacing, previewFrameTexture, cameraTargetResolution());
        AndroidAssetUtil.initializeNativeAssetManager(this.context);
    }


    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    protected void onPreviewDisplaySurfaceChanged(
            SurfaceHolder holder, int format, int width, int height) {
        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        boolean isCameraRotated = cameraHelper.isCameraRotated();
        converter.setDestinationSize(
                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    }

    private void setupPreviewDisplayView() {
        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                onPreviewDisplaySurfaceChanged(holder, format, width, height);
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }

}
