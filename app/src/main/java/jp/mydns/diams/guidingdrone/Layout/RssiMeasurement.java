package jp.mydns.diams.guidingdrone.Layout;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import jp.mydns.diams.guidingdrone.Common.GuidingDroneApplication;
import jp.mydns.diams.guidingdrone.R;

public class RssiMeasurement extends Fragment {

    private View view;
    private MainActivity mParent;

    private Button mTakeOff_btn, mStop_btn;
    private TextView mRssi_txt, mFileName_txt;
    private TextView mIsConneted_txt;

    private FlightController mFlightController;
    private DisplayUpdateHandler mDisplayUpdateHandler;

    private double[] mRssi;
    private List<String> mFileName;
    private boolean mIsConnected;

    public RssiMeasurement() {
        // Required empty public constructor
    }

    public static RssiMeasurement newInstance() {
        RssiMeasurement fragment = new RssiMeasurement();
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
        view = inflater.inflate(R.layout.fragment_rssi_measurement, container, false);
        mParent = (MainActivity)getActivity();

        mTakeOff_btn = (Button) view.findViewById(R.id.rssi_measurement_take_off_button);
        mTakeOff_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mFlightController.sendDataToOnboardSDKDevice("start\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    });
                }
            }
        });

        mStop_btn = (Button) view.findViewById(R.id.rssi_measurement_stop_button);
        mStop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mFlightController.sendDataToOnboardSDKDevice("stop\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    });
                }
            }
        });

        mRssi_txt = (TextView) view.findViewById(R.id.rssi_measurement_rssi_textView);
        mFileName_txt = (TextView) view.findViewById(R.id.rssi_measurement_filename_textView);
        mIsConneted_txt = (TextView) view.findViewById(R.id.rssi_measurement_isconnected_textView);

        mRssi = new double[5];
        mFileName = new ArrayList<>();
        mIsConnected = false;

        initFlightController();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        mDisplayUpdateHandler = new DisplayUpdateHandler();
        mDisplayUpdateHandler.sleep(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        mDisplayUpdateHandler = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

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

            if (data.indexOf("rssi:") == 0) {
                String[] rssi_str = data.split(":")[1].split(",");
                mRssi = new double[rssi_str.length];
                for (int i = 0; i < rssi_str.length; i++) {
                    mRssi[i] = Double.parseDouble(rssi_str[i]);
                }
            } else if (data.indexOf("filename:") == 0) {
                String filename = data.split(":")[1];
                mFileName.add(filename);
            } else if (data.indexOf("isConnected:") == 0) {
                String isConnected = data.split(":")[1];
                mIsConnected = Boolean.parseBoolean(isConnected);
            } else {
                mParent.showToast(data);
            }
        }
    };

    private class DisplayUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mRssi != null) {
                String rssi_str = "RSSI：\n";
                for (int i = 0; i < mRssi.length; i++) {
                    rssi_str += String.format("ビーコン%d：%f\n", i + 1, mRssi[i]);
                }
                mRssi_txt.setText(rssi_str);
            }

            if (mFileName != null) {
                String filename = "ファイル名：\n";
                for (int i = 0; i < mFileName.size(); i++) {
                    filename += String.format("%d回目：%s\n", i + 1, mFileName.get(i));
                }
                mFileName_txt.setText(filename);
            }

            mIsConneted_txt.setText("isConnected：" + mIsConnected);

            if (mFlightController != null && !mIsConnected) {
                mFlightController.sendDataToOnboardSDKDevice("isConnected\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });
            }

            if (mDisplayUpdateHandler != null) {
                sleep(500);
            }
        }

        public void sleep(long delay) {
            removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delay);
        }
    }
}
