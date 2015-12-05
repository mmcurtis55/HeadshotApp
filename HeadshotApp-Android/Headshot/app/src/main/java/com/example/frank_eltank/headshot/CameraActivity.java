package com.example.frank_eltank.headshot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mFrame;

    private Button mButtonShare;
    private Button mButtonSave;
    private Button mButtonCancel;
    private boolean mPreviewLocked = false;

    private byte[] mPictureData;
    private File mPictureFile;

    private void makeToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /** A safe way to get an instance of the Camera object. */
    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        c.setDisplayOrientation(90);
        Camera.Parameters params = c.getParameters();
        params.setRotation(270);
        c.setParameters(params);
        return c; // returns null if camera is unavailable
    }

    /***
     * Initializes the Cancel, Share, Save buttons
     */
    private void initButtons(){
        mButtonCancel = (Button) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCamera.startPreview();
                mPreviewLocked = false;
                toggleButtons(false);
                mPictureData = null;
            }
        });

        mButtonShare = (Button) findViewById(R.id.button_share);

        mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveJPEG();
            }
        });
    }

    /***
     * Toggles the visibilty of the Cancel, Share, Save buttons
     * @param show: true to show, false to hide
     */
    private void toggleButtons(boolean show){
        if(show){
            mButtonCancel.setVisibility(View.VISIBLE);
            mButtonShare.setVisibility(View.VISIBLE);
            mButtonSave.setVisibility(View.VISIBLE);
        }
        else{
            mButtonCancel.setVisibility(View.INVISIBLE);
            mButtonShare.setVisibility(View.INVISIBLE);
            mButtonSave.setVisibility(View.INVISIBLE);
        }
        mFrame.requestLayout();
    }

    /***
     * Saves a JPEG of the base picture taken
     * TODO: Possible optimization needed to process overlay in parallel
     */
    private void saveJPEG(){
        if(mPictureData == null){
            makeToast("Picture data was not saved!");
            return;
        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Headshot");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                makeToast("Error creating Headshot directory!");
                return;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mPictureFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");

        if (mPictureFile == null) {
            makeToast("Could not create file");
            return;
        }

        Bitmap cameraBitmap = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length);
        Matrix matrix = new Matrix();
        // Camera view orientation is portrait BUT picture taken buffer is landscape
        matrix.postRotate(90);
        // Don't mirror image
        matrix.preScale(-1, 1);
        Bitmap cameraPostEffects = Bitmap.createBitmap(cameraBitmap , 0, 0, cameraBitmap.getWidth(), cameraBitmap.getHeight(), matrix, true);

        // Create a new Bitmap and Canvas to draw our camera picture and overlay on
        Bitmap newImage = Bitmap.createBitmap(cameraPostEffects.getWidth(), cameraPostEffects.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newImage);
        // Draw picture
        canvas.drawBitmap(cameraPostEffects, 0f, 0f, null);

        // Draw overlay
        Drawable overlay = getResources().getDrawable(R.drawable.suit_nd);
        overlay.setBounds(0,0,cameraPostEffects.getWidth(), cameraPostEffects.getHeight());
        overlay.draw(canvas);

        // Saved merged overlay
        try {
            FileOutputStream fos = new FileOutputStream(mPictureFile);
            newImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            makeToast("File not found!");
        } catch (IOException e) {
            makeToast("Unable to save image data!");
        }
        makeToast("Headshot Saved!");

        MediaScannerConnection.scanFile(this, new String[]{mPictureFile.toString()}, null, null);
    }

    /***
     * Callback when the picture is taken
     */
    public Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){
        @Override
        public void onShutter() {
            makeToast("Picture Taken!");
            mPreviewLocked = true;
            toggleButtons(true);
        }
    };

    /***
     * Callback when the raw data is available
     */
    private Camera.PictureCallback mRawCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

        }
    };

    /***
     * Callback when the JPEG data is available
     */
    private Camera.PictureCallback mJPEGCallback = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            mPictureData = bytes.clone();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Initialize Buttons
        initButtons();

        // Create an instance of Camera
        mCamera = getCameraInstance();

        if(mCamera == null){
            makeToast("Camera is null");
        }
        else{
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this,this, mCamera);
            mFrame= (FrameLayout) findViewById(R.id.camera_preview);
            mFrame.addView(mPreview);
        }

        SurfaceView mView = (SurfaceView) findViewById(R.id.overlay);
        mView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //mFrame.setBackgroundResource(R.drawable.suit_nd);

        // Taking a picture via tap gesture
        mPreview.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(mPreviewLocked){
            mCamera.startPreview();
            mPreviewLocked = false;
            toggleButtons(false);
        }
        else{
            super.onBackPressed();
        }
    }
}
