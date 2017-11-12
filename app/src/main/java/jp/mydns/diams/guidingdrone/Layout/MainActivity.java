package jp.mydns.diams.guidingdrone.Layout;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import jp.mydns.diams.guidingdrone.Common.GuidingDroneApplication;
import jp.mydns.diams.guidingdrone.R;

public class MainActivity extends AppCompatActivity {

    private TextView mConnectStatus_txtview;

    public GuidingDroneApplication getGuidingDroneApplication() {
        return GuidingDroneApplication.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_main);

        mConnectStatus_txtview = (TextView)findViewById(R.id.activity_connectStatus_textView);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, new SpeedSettingFragment(), SpeedSettingFragment.class.getName());
            transaction.commit();
        }
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTitleBar();
        }
    };

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateTitleBar() {
        if(mConnectStatus_txtview == null) return;
        boolean ret = false;
        BaseProduct product = GuidingDroneApplication.getProductInstance();
        if (product != null) {
            if(product.isConnected()) {
                //The product is connected
                mConnectStatus_txtview.setText(GuidingDroneApplication.getProductInstance().getModel() + " Connected");
                ret = true;
            } else {
                if(product instanceof Aircraft) {
                    Aircraft aircraft = (Aircraft)product;
                    if(aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                        // The product is not connected, but the remote controller is connected
                        mConnectStatus_txtview.setText("only RC Connected");
                        ret = true;
                    }
                }
            }
        }
        if(!ret) {
            // The product or the remote controller are not connected.
            mConnectStatus_txtview.setText("Disconnected");
        }
    }

    @Override
    public void onAttachedToWindow() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GuidingDroneApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        Log.e(MainActivity.class.getName(), "onResume");
        super.onResume();
        updateTitleBar();
    }

    @Override
    protected void onDestroy() {
        Log.e(MainActivity.class.getName(), "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
