package com.example.frank_eltank.headshot;

/***
 *
 * Author: Frank Lin
 * Email: fylin134@gmail.com
 *
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/***
 *
 * CameraPreview class. This is what displays what the camera currently sees.
 *
 * In addition, all camera functionality is controlled in here
 *
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Context mContext;
    private SurfaceHolder mHolder;
    private String TAG = "Sample Headshot";

    /***
     * Public Constructor
     * @param context
     */
    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.setKeepScreenOn(true);
    }

    // ***** Callbacks for SurfaceHolder.Callback *****
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            CameraActivity.sCamera.setPreviewDisplay(holder);
            CameraActivity.sCamera.startPreview();
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
            CameraActivity.sCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            CameraActivity.sCamera.setPreviewDisplay(mHolder);
            CameraActivity.sCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }
}