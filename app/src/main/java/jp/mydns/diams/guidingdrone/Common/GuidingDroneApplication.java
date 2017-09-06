package jp.mydns.diams.guidingdrone.Common;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.log.DJILog;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import jp.mydns.diams.guidingdrone.R;

import static android.R.attr.key;

/**
 * Created by daimon on 2017/06/05.
 */

public class GuidingDroneApplication extends Application {

    private static final String TAG = GuidingDroneApplication.class.getName();

    public static final String FLAG_CONNECTION_CHANGE = "jp_mydns_diams_guidingdrone_connect_change";

    private static BaseProduct mProduct;
    private static GuidingDroneApplication instance;

    private Handler mHandler;

    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) return null;
        return (Aircraft) getProductInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler(Looper.getMainLooper());
        /**
         * handles SDK Registration using the API_KEY
         */
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck == 0 || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            DJISDKManager.getInstance().registerApp(this, mDJISDKManagerCallback);
        }
        instance = this;
    }

    public static GuidingDroneApplication getInstance() {
        return instance;
    }

    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {

        @Override
        public void onRegister(DJIError djiError) {
            if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                DJILog.e("App registration", DJISDKError.REGISTRATION_SUCCESS.getDescription());
                DJISDKManager.getInstance().startConnectionToProduct();
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), R.string.sdk_registration_message, Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            Log.v(TAG, djiError.getDescription());
        }

        @Override
        public void onProductChange(BaseProduct baseProduct, BaseProduct baseProduct1) {
            Log.d(TAG, String.format("onProductChanged oldProduct:%s, newProduct:%s", baseProduct, baseProduct1));
            Toast.makeText(getApplicationContext(), String.format("onProductChanged oldProduct:%s, newProduct:%s", baseProduct, baseProduct1), Toast.LENGTH_LONG).show();
            mProduct = baseProduct1;
            if (mProduct != null) {
                mProduct.setBaseProductListener(mDJIBaseProductListener);
            }

            notifyStatusChange();
        }
    };

    private BaseProduct.BaseProductListener mDJIBaseProductListener = new BaseProduct.BaseProductListener() {

        @Override
        public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent baseComponent, BaseComponent baseComponent1) {
            if (baseComponent1 != null) {
                baseComponent1.setComponentListener(mDJIComponentListener);
            }
            Log.d(TAG,
                    String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                            key,
                            baseComponent,
                            baseComponent1));

            notifyStatusChange();
        }

        @Override
        public void onConnectivityChange(boolean b) {
            Log.d(TAG, "onProductConnectivityChanged: " + b);

            notifyStatusChange();
        }
    };

    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean b) {
            Log.d(TAG, "onComponentConnectivityChanged: " + b);
            notifyStatusChange();
        }
    };

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };
}
