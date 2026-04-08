package com.payoda.smartlock.plugins.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.payoda.smartlock.BuildConfig;
import com.payoda.smartlock.constants.ServiceUrl;
import com.payoda.smartlock.plugins.storage.SecuredStorageManager;
import com.payoda.smartlock.plugins.storage.StorageManager;
import com.payoda.smartlock.plugins.wifi.WifiLockManager;
import com.payoda.smartlock.splash.model.BrandInfoResponse;
import com.payoda.smartlock.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.CONNECTIVITY_SERVICE;

import javax.net.ssl.SSLSocketFactory;

/**
 * This class hold the service related functionality like making http request in
 * REST API standard and return JSON Object or String as a response.
 * Created by david.
 */
public class ServiceManager {

    private static final String TAG = "Smartlock";
    private RequestQueue mRequestQueue;
    private static ServiceManager mInstance;
    private String token;
    private Context context = null;
    private static NetworkRegister networkRegister;
    private String previousUrl = "";

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native String getPublicKey(String buildVariant, String bundleId);

    public static final String PUBLIC_KEY = getPublicKey(BuildConfig.BUILD_VARIANT, BuildConfig.APPLICATION_ID);

    private ServiceManager() {
    }

    public void init(Context context) {
        this.context = context;
        if (mRequestQueue == null) {
            //mRequestQueue = Volley.newRequestQueue(context);
            mRequestQueue = Volley.newRequestQueue(context, new HurlStack(null, pinnedSSLSocketFactory()));
            try {
                setToken(SecuredStorageManager.getInstance().getToken());
                Logger.i(token);
            } catch (Exception e) {
                Logger.e(e);
            }
        }
    }

    public static synchronized ServiceManager getInstance() {
        if (mInstance == null) {
            mInstance = new ServiceManager();
        }
        return mInstance;
    }

    private SSLSocketFactory pinnedSSLSocketFactory() {
        try {
            return new TLSSocketFactory(PUBLIC_KEY);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private <T> void addToRequestQueue(Request<T> req, String tag) {

        int initialTimeoutMs = 2000;  // Initial timeout (5 seconds)
        int maxNumRetries = 1;        // Maximum number of retries
        float backoffMultiplier = 1.0f;  // Backoff multiplier

        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        // req.setRetryPolicy(new DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier));
        req.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // req.setShouldCache(false); // Disable caching, which might interfere with redirects
        mRequestQueue.add(req);

    }

    private <T> void addToRequestQueue(Request<T> req) {
        addToRequestQueue(req, TAG);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private Map<String, String> getAuthHeaders() {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");

        // headers.put("bundle_id", "com.astrixengineering.touchplus.dev");
        // headers.put("bundle_id", "com.payoda.secnor.dev");
        // headers.put("bundle_id", BuildConfig.APPLICATION_ID);

        headers.put("bundle-id", BuildConfig.APPLICATION_ID);
        if (!TextUtils.isEmpty(token))
            headers.put("Authorization", getToken());

        Logger.d("### Headers --> ", headers.toString());
        return headers;
    }

    private String getErrorMessage(VolleyError error) {

        String errorMessage = "Oops! Something went wrong.";
        if (error == null)
            return errorMessage;
        if (error instanceof NoConnectionError) {
            errorMessage = "No Internet. Please Check your network connection.";
        } else if (error instanceof AuthFailureError) {
            errorMessage = parseErrorMessage(error.networkResponse, "Session Expired. Please Login back");
        } else if (error instanceof TimeoutError) {
            errorMessage = "Session timeout. Please check your internet connection.";
        } else if (error instanceof ServerError) {
            errorMessage = parseErrorMessage(error.networkResponse, "Server error. Please try again later.");
        } else if (error instanceof NetworkError) {
            errorMessage = "Network error. Please try again later.";
        } else if (error instanceof ParseError) {
            errorMessage = "Parser error. Please try again later.";
        } else {
            errorMessage = parseErrorMessage(error.networkResponse, errorMessage);
        }

        return errorMessage;
    }

    private String parseErrorMessage(NetworkResponse response, String defaultMessage) {

        Logger.d("### Volley", "Response code: " + response.statusCode);
        Logger.d("### Volley", "Response headers: " + response.headers);

        if (response != null && response.data != null) {
            try {
                defaultMessage = new String(response.data, "UTF-8");
                JSONObject jsonObject = new JSONObject(defaultMessage);
                if (jsonObject.has("message")) {
                    defaultMessage = jsonObject.getString("message");
                } else if (jsonObject.has("messages")) {
                    defaultMessage = jsonObject.getString("messages");
                } else if (jsonObject.has("error")) {
                    defaultMessage = jsonObject.getString("error");
                } else if (jsonObject.has("errors")) {
                    Object intervention = jsonObject.getString("errors");
                    if (intervention instanceof JSONArray) {
                        defaultMessage = ((JSONArray) intervention).toString();
                    } else {
                        defaultMessage = jsonObject.getString("errors");
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultMessage;
    }

    public String getErrorMessage(Exception error) {
        String errorMessage = "Oops! Something went wrong.";
        if (error == null)
            return errorMessage;
        if (error instanceof JsonSyntaxException) {
            errorMessage = "Parser error. Please try again later.";
        }
        return errorMessage;
    }

  /*  public boolean isMobileDataEnabled(Context context) {
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }*/

    public boolean isMobileDataEnabled(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNetworkAvailable(Context context) {
        try {
            // Using ConnectivityManager to check for Network Connection
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);

            NetworkInfo activeNetworkInfo = null;
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager
                        .getActiveNetworkInfo();
            }
            String wifiName = WifiLockManager.getInstance().getConnectedWifiName(context);
            wifiName = wifiName != null ? wifiName : "";
            String wifiNameString = BuildConfig.MANUFACTURER_CODE;
            BrandInfoResponse.BrandInfo brandInfo = SecuredStorageManager.getInstance().getBrandInfo();
            if (brandInfo != null && !brandInfo.getManufacturerCode().isEmpty()) {
                wifiNameString = brandInfo.getManufacturerCode();
            }
            return (activeNetworkInfo != null && (!wifiName.startsWith(wifiNameString.toLowerCase())));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void enablePreferredNetwork(int method, String url, Object object, final ResponseHandler handler, String tag) {

        int[] capabilities = new int[]{NetworkCapabilities.NET_CAPABILITY_INTERNET};
        int[] transportTypes;
        previousUrl = url;

        if (url.contains(ServiceUrl.WIFI_LOCK_SERVICE_URL) && isWifiConnected()) {
            Logger.d("### Preferred Network already set : WIFI");
            request(method, url, object, handler, tag);
        } else if (url.contains(ServiceUrl.WIFI_LOCK_SERVICE_URL)) {
            if (url.contains(ServiceUrl.WIFI_LOCK_SERVICE_URL)) {
                // Add any NetworkCapabilities.TRANSPORT_...
                transportTypes = new int[]{NetworkCapabilities.TRANSPORT_WIFI};
            } else {
                if (WifiLockManager.getInstance().isWifiLockConnected(context)) {
                    transportTypes = new int[]{NetworkCapabilities.TRANSPORT_CELLULAR};
                } else {
                    transportTypes = new int[]{NetworkCapabilities.TRANSPORT_CELLULAR, NetworkCapabilities.TRANSPORT_WIFI};
                }
            }
            NetworkRequest.Builder networkRequest = new NetworkRequest.Builder();
            // add capabilities
            for (int cap : capabilities) {
                networkRequest.addCapability(cap);
            }
            // add transport types
            for (int trans : transportTypes) {
                networkRequest.addTransportType(trans);
            }
            connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
            if (networkRegister == null) {
                networkRegister = new NetworkRegister();
            }
            networkRegister.set(method, url, object, handler, tag);
            connectivityManager.registerNetworkCallback(networkRequest.build(), networkRegister);
            try {
                connectivityManager.unregisterNetworkCallback(networkRegister);
            } catch (Exception e) {
                Logger.d("### Unregistered Failed");
            }
            try {
                Thread.sleep(1000);
                request(method, url, object, handler, tag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            request(method, url, object, handler, tag);
        }

    }

    private void request(int method, String url, Object object, final ResponseHandler handler, String tag) {
        try {

            Logger.d("### URL ", url);
            //HttpsTrustManager.allowAllSSL();
            String data = (object != null) ? new Gson().toJson(object) : null;
            Logger.d("### Payloads ", data);

            try {
                JSONObject payload = (data != null) ? new JSONObject(data) : null;
                JsonObjectRequest request = new JsonObjectRequest(method, url, payload, response -> {

                    Logger.d("### Res URL ", url);
                    Logger.i("### ", " Response  -> " + response.toString());

                    if (handler != null) {
                        handler.onSuccess(response.toString());
                    }
                }, error -> {

                    String message = getErrorMessage(error);

                    Logger.d("### Res URL ", url);
                    Logger.d("### ", " Response  error -> " + message);

                    try {
                        Logger.d("### ", " Response  error Object-> " + error.getMessage());
                        Logger.d("### ", " Response  error Object-> " + error.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (handler != null) {
                        if (error instanceof AuthFailureError) {
                            handler.onAuthError(message);
                        } else {
                            handler.onError(message);
                        }
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return getAuthHeaders();
                    }
                };
                addToRequestQueue(request, tag);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            Logger.e(e);
            if (handler != null) {
                handler.onError(e.getMessage());
            }
        }
    }

    public void get(String url, Object data, final ResponseHandler handler) {
        get(url, data, handler, TAG);
        Logger.d("### METHOD TYPE  : GET ");
    }

    public void get(String url, Object data, final ResponseHandler handler, String tag) {
        enablePreferredNetwork(Request.Method.GET, url, data, handler, tag);
    }

    public void post(String url, Object data, final ResponseHandler handler) {
        post(url, data, handler, TAG);
    }

    public void post(String url, Object data, final ResponseHandler handler, String tag) {
        enablePreferredNetwork(Request.Method.POST, url, data, handler, tag);
        Logger.d("### METHOD TYPE  : POST ");
    }

    public void patch(String url, Object data, final ResponseHandler handler) {
        patch(url, data, handler, TAG);
        Logger.d("### METHOD TYPE  : PATCH ");
    }

    public void patch(String url, Object data, final ResponseHandler handler, String tag) {
        enablePreferredNetwork(Request.Method.PATCH, url, data, handler, tag);
    }

    public void delete(String url, Object data, final ResponseHandler handler) {
        delete(url, data, handler, TAG);
        Logger.d("### METHOD TYPE  : DELETE ");
    }

    public void delete(String url, Object data, final ResponseHandler handler, String tag) {
        enablePreferredNetwork(Request.Method.DELETE, url, data, handler, tag);
    }

    private static ConnectivityManager connectivityManager = null;

    private boolean isWifiConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return true;
            }
        }
        return false;
    }

    private class NetworkRegister extends ConnectivityManager.NetworkCallback {
        private String url, tag;
        private int method;
        private Object object;
        private ResponseHandler handler;

        public void set(int method, String url, Object object, ResponseHandler handler, String tag) {
            this.method = method;
            this.url = url;
            this.object = object;
            this.handler = handler;
            this.tag = tag;
        }

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            Logger.d("onAvailable Cur" + url);
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            Logger.d("onUnavailable");
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            Logger.d("onLost");
        }
    }

}

