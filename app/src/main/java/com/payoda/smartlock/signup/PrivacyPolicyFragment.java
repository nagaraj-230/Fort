package com.payoda.smartlock.signup;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.R;
import com.payoda.smartlock.authentication.BaseFragment;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.Logger;

/**
 * Created by david on 09/10/18.
 */

public class PrivacyPolicyFragment extends BaseFragment {

    public static final String  TAG = "### PrivacyPolicyFragment";

    public PrivacyPolicyFragment() {
        // Required empty public constructor
    }

    public static PrivacyPolicyFragment getInstance() {
        return new PrivacyPolicyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Logger.d(TAG, TAG);
        return initializeView(inflater.inflate(R.layout.fragment_terms_conditions, container, false));
    }

    private View initializeView(View view){
        ((TextView) view.findViewById(R.id.tv_title)).setText("Privacy Policy");
        WebView webView=view.findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                return false;
            }
        });
        // commented by karunya
        /*if(brandInfo != null){
            webView.loadUrl(brandInfo.getPrivacyUrl());
        }else {*/
            webView.loadUrl(BuildConfig.PRIVACY_URL);
        //}

        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null)
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();

            }
        });
        return view;
    }
}
