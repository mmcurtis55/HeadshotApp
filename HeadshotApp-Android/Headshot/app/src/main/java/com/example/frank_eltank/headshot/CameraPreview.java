package com.example.frank_eltank.headshot;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Context mContext;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private String TAG = "Sample Headshot";

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){
        @Override
        public void onShutter() {
            Toast.makeText(mContext, "Picture taken!", Toast.LENGTH_SHORT).show();
            CameraActivity.sPreviewLocked = true;
        }
    };

    private Camera.PictureCallback mRawCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

        }
    };

    private Camera.PictureCallback mJPEGCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

        }
    };

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.setKeepScreenOn(true);

        // Taking a picture via tap gesture
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Callbacks available to pass to this function are in the following order:
                // shutter - callback for image capture moment
                // raw - callback for raw (uncompressed) image data
                // postview - callback with postview image data
                // jpeg - callback for JPEG image data
                mCamera.takePicture(mShutterCallback, mRawCallback, null, mJPEGCallback);
            }
        });
    }

    // ***** Callbacks for SurfaceHolder.Callback *****
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}