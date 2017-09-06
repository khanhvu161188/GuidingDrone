package jp.mydns.diams.guidingdrone.Layout;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import jp.mydns.diams.guidingdrone.Common.GuidingDroneApplication;
import jp.mydns.diams.guidingdrone.R;

public class DataReceiveFragment extends Fragment {

    private View view;
    private MainActivity mParent;

    private String mData;
    private List<String> mBeaconID;

    private TextView mProximityBeaconID_txtview;
    private ListView mDetectedBeaconID_lst;
    private TextView mIsOnboardSDKDeviceAvailable_txtview;

    private FlightController mFlightController;

    private RedrawHandler mRedraw;

    public DataReceiveFragment() {
        // Required empty public constructor
    }

    public static DataReceiveFragment newInstance(String param1, String param2) {
        DataReceiveFragment fragment = new DataReceiveFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_data_receive, container, false);
        mParent = (MainActivity) getActivity();

        mData = "";
        mBeaconID = new ArrayList<>();

        mProximityBeaconID_txtview = (TextView) view.findViewById(R.id.data_receive_proximityBeaconID_textView);
        mDetectedBeaconID_lst = (ListView) view.findViewById(R.id.data_receive_detectedBeaconIDs_listView);
        mIsOnboardSDKDeviceAvailable_txtview = (TextView) view.findViewById(R.id.data_receive_isOnboardSDKDeviceAvailable_textview);

        initFlightController();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mRedraw = new RedrawHandler();
        mRedraw.sleep(0);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRedraw = null;
    }

    private FlightController.OnboardSDKDeviceDataCallback onboardSDKDeviceDataCallback = new FlightController.OnboardSDKDeviceDataCallback() {
        @Override
        public void onReceive(byte[] bytes) {
            String data;
            try {
                data = new String(bytes, "SJIS");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                data = e.getMessage() + "\n";
            }
            mData = data;
        }
    };

    private void initFlightController() {
        Aircraft aircraft = GuidingDroneApplication.getAircraftInstance();
        if (aircraft == null || !aircraft.isConnected()) {
            mParent.showToast("Disconnected");
            mFlightController = null;
        } else {
            mFlightController = aircraft.getFlightController();
            mFlightController.setOnboardSDKDeviceDataCallback(onboardSDKDeviceDataCallback);
        }
    }

    private class RedrawHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mFlightController != null) {
                boolean isOnboardSDKDeviceAvailable = mFlightController.isOnboardSDKDeviceAvailable();
                mIsOnboardSDKDeviceAvailable_txtview.setText("OSDK-Device : " + isOnboardSDKDeviceAvailable);
                if (isOnboardSDKDeviceAvailable) {
                    mIsOnboardSDKDeviceAvailable_txtview.setBackgroundColor(Color.WHITE);
                } else {
                    mIsOnboardSDKDeviceAvailable_txtview.setBackgroundColor(Color.RED);
                }

                mFlightController.sendDataToOnboardSDKDevice("ack\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });
            }

            String[] data = mData.split(",");

            if (data.length < 2) {
                if (mRedraw != null) {
                    mRedraw.sleep(1000);
                }
                return;
            }

            mProximityBeaconID_txtview.setText(data[0]);
            if (Boolean.valueOf(data[1])) {
                mProximityBeaconID_txtview.setBackgroundColor(Color.GREEN);
            } else {
                mProximityBeaconID_txtview.setBackgroundColor(Color.WHITE);
            }

            if (!mBeaconID.contains(data[0])) {
                mBeaconID.add(data[0]);
            }
            String[] beacons = mBeaconID.toArray(new String[mBeaconID.size()]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(mParent, android.R.layout.simple_expandable_list_item_1, beacons);
            mDetectedBeaconID_lst.setAdapter(adapter);

            if (mRedraw != null) {
                mRedraw.sleep(100);
            }
        }

        public void sleep(long delay) {
            removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delay);
        }
    }
}
