package com.payoda.smartlock.getstarted;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.payoda.smartlock.R;
import com.payoda.smartlock.utils.Logger;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetStartedFragment extends Fragment {

    public static final String TAG="### GetStartedFragment";


    public GetStartedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return inflater.inflate(R.layout.fragment_get_started, container, false);
    }

}
