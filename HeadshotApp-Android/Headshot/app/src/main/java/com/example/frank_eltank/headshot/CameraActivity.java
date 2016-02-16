package com.example.frank_eltank.headshot;

/***
 *
 * Author: Frank Lin
 * Email: fylin134@gmail.com
 *
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/***
 *
 * This is the main activity class.
 *
 * It handles UI and application state behaviors
 *
 */
public class CameraActivity extends Activity {

    static Camera sCamera;
    private CameraPreview mPreview;
    private DrawablesCircularArray mCutouts;

    // Handles to UI elements
    private RelativeLayout mContainer;
    private SurfaceView mCutoutView1;
    private SurfaceView mCutoutView2;
    private Button mButtonShare;
    private Button mButtonSave;
    private Button mButtonCancel;
    private Button mButtonShutter;
    //private Button mButtonFlash;
    //private RelativeLayout mSavedBanner;

    // True if the camera preview is paused
    private boolean mPreviewLocked = false;
    //private boolean mFlashOn = false;
    private boolean mShareNoToggleUI = false;
    private float mBrightness = 0.0f;
    private byte[] mPictureData;
    private File mPictureFile;

    // ****************************************************
    // ***** UI Initialization and Behavior Functions *****
    // ****************************************************

    /***
     * Initializes the Cancel, Share, Save buttons
     */
    private void initUI(){
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mButtonCancel = (Button) findViewById(R.id.button_cancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sCamera.startPreview();
                mPreviewLocked = false;
                togglePreviewUI(true);
                mPictureData = null;
                /*if(mFlashOn){
                    cleanupArtificialFlash();
                }*/
            }
        });

        mButtonShare = (Button) findViewById(R.id.button_share);
        mButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareHeadshot();
            }
        });

        mButtonSave = (Button) findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showSavedCrouton();
                makeToast("Saved!");
                saveHeadshot();
            }
        });

        mButtonShutter = (Button) findViewById(R.id.button_shutter);
        mButtonShutter.setBackgroundColor(Color.argb(0,255,255,255));
        mButtonShutter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(mFlashOn){
                    runArtificialFlash();
                    // Callbacks available to pass to this function are in the following order:
                    // shutter - callback for image capture moment
                    // raw - callback for raw (uncompressed) image data
                    // postview - callback with postview image data
                    // jpeg - callback for JPEG image data
                    final Handler handler = new Handler();

                    final Runnable r = new Runnable() {
                        public void run() {
                            sCamera.takePicture(mShutterCallback, mRawCallback, null, mJPEGCallback);
                        }
                    };

                    handler.postDelayed(r, 800);
                }
                else{*/
                    sCamera.takePicture(mShutterCallback, mRawCallback, null, mJPEGCallback);
                //}

            }
        });

        /*mButtonFlash = (Button) findViewById(R.id.button_flash);
        mButtonFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFlash(mFlashOn);
                mFlashOn = !mFlashOn;
                // Save current brightness
                mBrightness = getWindow().getAttributes().screenBrightness;
                if(mBrightness <0){
                    try {
                        mBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    } catch (Settings.SettingNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });*/

        //mSavedBanner = (RelativeLayout) findViewById(R.id.banner_saved);
        //SurfaceView flashOverlay = (SurfaceView) findViewById(R.id.overlay_flash);
        //flashOverlay.setBackgroundColor(Color.argb(125,255,255,255));
    }

    /***
     * Toggles the visibilty of the Cancel, Share, Save buttons
     * @param previewMode: true to show only shutter button, false to show save/share/cancel buttons
     */
    private void togglePreviewUI(boolean previewMode){
        if(previewMode){
            mButtonCancel.setVisibility(View.INVISIBLE);
            mButtonShare.setVisibility(View.INVISIBLE);
            mButtonSave.setVisibility(View.INVISIBLE);
            mButtonShutter.setVisibility(View.VISIBLE);
            //mButtonFlash.setVisibility(View.VISIBLE);
        }
        else{
            mButtonCancel.setVisibility(View.VISIBLE);
            mButtonShare.setVisibility(View.VISIBLE);
            mButtonSave.setVisibility(View.VISIBLE);
            mButtonShutter.setVisibility(View.INVISIBLE);
            //mButtonFlash.setVisibility(View.INVISIBLE);
        }
        mContainer.requestLayout();
    }

    /***
     * Toggles the flash UI element look and
     * the flash logic behind
     * @param flashToggled - TRUE if flash is currently ON
     */
    private void toggleFlash(boolean flashToggled){
        if(flashToggled){
            //mButtonFlash.setBackgroundResource(R.drawable.flash_off);
        }
        else{
            //mButtonFlash.setBackgroundResource(R.drawable.flash_on);
        }
    }

    /***
     * shareHeadshot
     */
    private void shareHeadshot(){
        if(mPictureFile == null){
            saveHeadshot();
        }

        // Sharing causes the app to put into PAUSE state which toggles UI
        // But the UX spec requires the share, save, and X buttons to NOT disappear
        // when share is tapped
        mShareNoToggleUI = true;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), "com.myfileprovider", mPictureFile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        startActivity(Intent.createChooser(shareIntent, "Share Headshot"));
        /*if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image*//*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mPictureFile.getAbsolutePath()));
            startActivity(Intent.createChooser(shareIntent, "Share Headshot"));
            makeToast("Android Kitkat- Share");
        }
        else{
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image*//*");
            Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), "com.myfileprovider", mPictureFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            startActivity(Intent.createChooser(shareIntent, "Share Headshot"));
            makeToast("Android Lollipop+ Share");
        }*/
    }

    /***
     * Saves a JPEG of the base picture taken
     * TODO: Possible optimization needed to process overlay in parallel
     */
    private void saveHeadshot(){
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
        mPictureFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpeg");

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
        Drawable overlay = getResources().getDrawable(mCutouts.getCurrentDrawable());
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
        MediaScannerConnection.scanFile(this, new String[]{mPictureFile.toString()}, null, null);

        // Cleanup
        cameraPostEffects.recycle();
        newImage.recycle();
        cameraBitmap.recycle();
    }


    // ****************************************************
    // ***************** Camera Functions *****************
    // ****************************************************

    /**
     * Calculate the optimal size of camera preview
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes)
        {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        SharedPreferences previewSizePref;
        previewSizePref = getSharedPreferences("FRONT_PREVIEW_PREF",MODE_PRIVATE);

        SharedPreferences.Editor prefEditor = previewSizePref.edit();
        prefEditor.putInt("width", optimalSize.width);
        prefEditor.putInt("height", optimalSize.height);

        prefEditor.commit();

        return optimalSize;
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

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Camera.Size optimalSize = getOptimalSize(params.getSupportedPreviewSizes(), size.x, size.y);
        params.setPreviewSize(optimalSize.width, optimalSize.height);
        params.setPictureSize(optimalSize.width, optimalSize.height);
        c.setParameters(params);
        return c; // returns null if camera is unavailable
    }

    /***
     * Sets the screen to a white background and
     * increases brightness to maximum to create
     * an artificial flash
     */
/*    private void runArtificialFlash(){
        Window window = getWindow();

        // Enable white background flash overlay
        //SurfaceView flashOverlay = (SurfaceView) findViewById(R.id.overlay_flash);
        //flashOverlay.setVisibility(View.VISIBLE);

        // Get current brightness
        try {
            mBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            makeToast("Brightness not available");
            e.printStackTrace();
        }

        //Set the system brightness using the brightness variable value
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        //Set the brightness of this window
        layoutpars.screenBrightness = 100 / (float)100;
        window.setAttributes(layoutpars);
        mContainer.requestLayout();
    }*/

    /***
     * Clear white flash overlay and
     * readjust screen brightness to user's previous brightness
     */
/*    private void cleanupArtificialFlash(){
        Window window = getWindow();

        //Set the system brightness using the brightness variable value
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) mBrightness);
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        //Set the brightness of this window
        layoutpars.screenBrightness = mBrightness / 100;
        window.setAttributes(layoutpars);
    }*/

    /***
     * Callback when the picture is taken
     */
    public Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){
        @Override
        public void onShutter() {
            mPreviewLocked = true;
            togglePreviewUI(false);
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
            // Get a copy of the picture byte data
            mPictureData = bytes.clone();

            // Once the JPEG is available, we can turn off the flash overlay
            Drawable image = new BitmapDrawable(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
           // SurfaceView flashOverlay = (SurfaceView) findViewById(R.id.overlay_flash);
            //flashOverlay.setVisibility(View.GONE);
        }
    };


    // ****************************************************
    // ************* Helper/Utility Functions *************
    // ****************************************************

    /***
     * Toast helper function
     * @param message: The message to toast
     */
    private void makeToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    /***
     * Shows a Crouton (a toast like message that slides from the top of the screen)
     * indicating the photo was saved
     */
/*    private void showSavedCrouton(){
        mSavedBanner = (RelativeLayout) findViewById(R.id.banner_saved);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mSavedBanner.startAnimation(slideDown);
    }*/

    // ****************************************************
    // ********** Application Behavior Functions **********
    // ****************************************************

    /***
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Initialize Buttons
        initUI();

        // Create an instance of Camera
        sCamera = getCameraInstance();

        if(sCamera == null){
            makeToast("Camera is null");
        }
        else{
            // Create our Preview view and set it as the content of our activity.
            mPreview = (CameraPreview) findViewById(R.id.camera_preview);
        }

        mCutoutView1 = (SurfaceView) findViewById(R.id.overlay1);
        mCutoutView1.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mCutoutView2 = (SurfaceView) findViewById(R.id.overlay2);
        mCutoutView2.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mCutouts = new DrawablesCircularArray();

        // Enable cutout swiping
        mCutoutView1.setOnTouchListener(getCutoutSwipeListener(mCutoutView1));
        mCutoutView2.setOnTouchListener(getCutoutSwipeListener(mCutoutView2));
    }


    private OnSwipeTouchListener getCutoutSwipeListener(final SurfaceView view){
        return new OnSwipeTouchListener(getApplicationContext()){
            @Override
            public void onSwipeRight(){
                if(!mPreviewLocked) {
                    if (mCutoutView1.equals(view)) {
                        // Hide the first cutout and change its cutout behind the scenes
                        Animation slideRightOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_out);
                        mCutoutView1.startAnimation(slideRightOut);
                        mCutoutView1.setVisibility(View.INVISIBLE);

                        // Show the second cutout with animation
                        mCutoutView2.setBackgroundResource(mCutouts.getPreviousDrawable());
                        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right);
                        mCutoutView2.startAnimation(slideDown);
                        mCutoutView2.setVisibility(View.VISIBLE);
                    } else {
                        // Hide the second cutout and change its cutout behind the scenes
                        Animation slideRightOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right_out);
                        mCutoutView2.startAnimation(slideRightOut);
                        mCutoutView2.setVisibility(View.INVISIBLE);


                        // Show the first cutout with animation
                        mCutoutView1.setBackgroundResource(mCutouts.getPreviousDrawable());
                        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right);
                        mCutoutView1.startAnimation(slideDown);
                        mCutoutView1.setVisibility(View.VISIBLE);
                    }
                }
                super.onSwipeRight();
            }

            @Override
            public void onSwipeLeft() {
                if(!mPreviewLocked){
                    if(mCutoutView1.equals(view)){
                        // Hide the first cutout and change its cutout behind the scenes
                        Animation slideLeftOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_out);
                        mCutoutView1.startAnimation(slideLeftOut);
                        mCutoutView1.setVisibility(View.INVISIBLE);

                        // Show the second cutout with animation
                        mCutoutView2.setBackgroundResource(mCutouts.getNextDrawable());
                        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left);
                        mCutoutView2.startAnimation(slideDown);
                        mCutoutView2.setVisibility(View.VISIBLE);
                    }
                    else{
                        // Hide the second cutout and change its cutout behind the scenes
                        //mCutoutView2.setVisibility(View.INVISIBLE);
                        Animation slideLeftOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left_out);
                        mCutoutView2.startAnimation(slideLeftOut);
                        mCutoutView2.setVisibility(View.INVISIBLE);

                        // Show the first cutout with animation
                        mCutoutView1.setBackgroundResource(mCutouts.getNextDrawable());
                        Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left);
                        mCutoutView1.startAnimation(slideDown);
                        mCutoutView1.setVisibility(View.VISIBLE);
                    }
                }
                super.onSwipeLeft();
            }
        };
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
    protected void onStop(){
        super.onStop();
        togglePreviewUI(true);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(!mShareNoToggleUI){
            togglePreviewUI(true);
        }
        mShareNoToggleUI = false;
    }

    @Override
    public void onBackPressed(){
        // We don't want to exit the app when the view is of the taken picture.
        // but rather clear the picture and the UI buttons
        if(mPreviewLocked){
            sCamera.startPreview();
            mPreviewLocked = false;
            togglePreviewUI(true);
            /*if(mFlashOn){
                cleanupArtificialFlash();
            }*/
        }
        else{
            moveTaskToBack(true);
        }
    }
}
