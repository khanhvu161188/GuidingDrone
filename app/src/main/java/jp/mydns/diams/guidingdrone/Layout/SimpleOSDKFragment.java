package jp.mydns.diams.guidingdrone.Layout;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import jp.mydns.diams.guidingdrone.Common.GuidingDroneApplication;
import jp.mydns.diams.guidingdrone.R;

public class SimpleOSDKFragment extends Fragment {

    private View view;
    private MainActivity mParent;

    private Button mStart_btn;
    private Button mStop_btn;
    private Button mControlled_btn;
    private Button mReset_btn;
    private Button mReboot_btn;
    private TextView mBeacon_txt;
    private TextView mCenterBeaconStatus_txt, mBeaconPostion_txt;

    private FlightController mFlightController;

    private boolean mResetOK;
    private String mBeaconID;
    private boolean mCenterBeaconStatus;
    private boolean mBeaconPostion;
    private DisplayUpdateHandler mDisplayUpdateHandler;

    public SimpleOSDKFragment() {
        // Required empty public constructor
    }

    public static SimpleOSDKFragment newInstance() {
        SimpleOSDKFragment fragment = new SimpleOSDKFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_simple_osdk, container, false);
        mParent = (MainActivity) getActivity();

        mStart_btn = (Button) view.findViewById(R.id.simple_osdk_start_button);
        mStart_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mStart_btn.setEnabled(false);
                    mStop_btn.setEnabled(true);
                    mReset_btn.setEnabled(true);
                    mFlightController.sendDataToOnboardSDKDevice("start\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    });
                }
            }
        });

        mStop_btn = (Button) view.findViewById(R.id.simple_osdk_stop_button);
        mStop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mReset_btn.setEnabled(true);
                    mFlightController.sendDataToOnboardSDKDevice("stop\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    });
                }
            }
        });

        mControlled_btn = (Button) view.findViewById(R.id.simple_osdk_controlled_button);
        mControlled_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mFlightController.sendDataToOnboardSDKDevice("remote\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    });
                }
            }
        });

        mReset_btn = (Button) view.findViewById(R.id.simple_osdk_reset_button);
        mReset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mResetOK) {
                    mStart_btn.setEnabled(true);
                    mResetOK = false;
                }
            }
        });

        mReboot_btn = (Button) view.findViewById(R.id.simple_osdk_reboot_button);
        mReboot_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mFlightController.sendDataToOnboardSDKDevice("reboot\0".getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    });
                }
            }
        });

        mBeacon_txt = (TextView) view.findViewById(R.id.simple_osdk_beacon_textView);
        mCenterBeaconStatus_txt = (TextView) view.findViewById(R.id.simple_osdk_centerBeaconStatus_textView);
        mBeaconPostion_txt = (TextView) view.findViewById(R.id.simple_osdk_comeDir_textView);

        mResetOK = false;
        mBeaconID = "N/A";

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
            //mParent.showToast(data);

            if (data.equals("landing")) {
                mResetOK = true;
                mParent.showToast(data);
            } else if (data.substring(0, 5).equals("BA000") || data.equals("none")) {
                mBeaconID = data;
                mParent.showToast(data);
            } else if (data.indexOf("center_beacon_status:") == 0) {
                mCenterBeaconStatus = Boolean.parseBoolean(data.split(":")[1]);
            } else if (data.indexOf("come_dir:") == 0) {
                mBeaconPostion = !Boolean.parseBoolean(data.split(":")[1]);
            } else if (data.indexOf("beacon_position:") == 0) {
                mBeaconPostion = Boolean.parseBoolean(data.split(":")[1]);
            } else {
                mParent.showToast(data);
            }
        }
    };

    private class DisplayUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mBeaconID.equals("none")) {
                mBeacon_txt.setText(mBeaconID);
                mBeacon_txt.setBackgroundColor(Color.RED);
            } else if (!mBeaconID.equals("N/A")) {
                mBeacon_txt.setText(mBeaconID);
                mBeacon_txt.setBackgroundColor(Color.GREEN);
            }

            if (mCenterBeaconStatus) {
                mCenterBeaconStatus_txt.setText("近づいているかどうか：近づいている");
            } else {
                mCenterBeaconStatus_txt.setText("近づいているかどうか：遠ざかっている");
            }

            if (mBeaconPostion) {
                mBeaconPostion_txt.setText("ビーコンがどちら側にあるか：右");
            } else {
                mBeaconPostion_txt.setText("ビーコンがどちら側にあるか：左");
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
