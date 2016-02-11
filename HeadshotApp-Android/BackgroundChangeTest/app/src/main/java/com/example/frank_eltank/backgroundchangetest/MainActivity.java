package com.example.frank_eltank.backgroundchangetest;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;


public class MainActivity extends Activity {

    boolean toggle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RelativeLayout overlay = (RelativeLayout) findViewById(R.id.overlay);

        // Enable cutout swiping
        overlay.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeRight() {
                //if(!mPreviewLocked){
                overlay.setBackgroundResource(R.drawable.family_of_mice_co);
                super.onSwipeRight();
                //}
            }

            @Override
            public void onSwipeLeft() {
                //if(!mPreviewLocked){
                overlay.setBackgroundResource(R.drawable.suit_nd);
                super.onSwipeLeft();
                //}
            }
        });

 /*       overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggle) {
                    overlay.setBackgroundResource(R.drawable.family_of_mice_co);
                    toggle = !toggle;
                } else {
                    overlay.setBackgroundResource(R.drawable.suit_nd);
                    toggle = !toggle;
                }
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
