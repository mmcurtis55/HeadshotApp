package com.example.frank_eltank.headshot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Frank on 2/24/2016.
 */
public class CutoutFragment extends Fragment {

    private int resourceId;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        Bundle args = getArguments();
        resourceId = args.getInt("resourceId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle){
        ImageView view = new ImageView(getActivity());
        view.setBackgroundResource(resourceId);
        return view;
    }
}
