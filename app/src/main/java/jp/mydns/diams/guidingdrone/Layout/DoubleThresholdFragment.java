package jp.mydns.diams.guidingdrone.Layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import jp.mydns.diams.guidingdrone.Common.GuidingDroneApplication;
import jp.mydns.diams.guidingdrone.R;

public class DoubleThresholdFragment extends Fragment {

    private View view;
    private MainActivity mParent;

    private  EditText mAverage1_edt, mThreshold1_1_edt, mThreshold2_1_edt;
    private Button mSetting1_btn;

    private  EditText mAverage2_edt, mThreshold1_2_edt, mThreshold2_2_edt;
    private Button mSetting2_btn;

    private FlightController mFlightController;

    public DoubleThresholdFragment() {
        // Required empty public constructor
    }

    public static DoubleThresholdFragment newInstance() {
        DoubleThresholdFragment fragment = new DoubleThresholdFragment();
        Bundle args = new Bundle();
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
        view = inflater.inflate(R.layout.fragment_double_threshold, container, false);
        mParent = (MainActivity) getActivity();

        mAverage1_edt = (EditText) view.findViewById(R.id.double_threshold_average_editText);
        mThreshold1_1_edt = (EditText) view.findViewById(R.id.double_threshold_threshold1_editText);
        mThreshold2_1_edt = (EditText) view.findViewById(R.id.double_threshold_threshold2_editText);
        mSetting1_btn = (Button) view.findViewById(R.id.double_threshold_setting1_button);
        mSetting1_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mSetting1_btn.setEnabled(false);

                    String average_str = mAverage1_edt.getText().toString();
                    String threshold1_str = mThreshold1_1_edt.getText().toString();
                    String threshold2_str = mThreshold2_1_edt.getText().toString();
                    String sendData_str = "configure:" + average_str + "," + threshold1_str + "," + threshold2_str + "\0";

                    mFlightController.sendDataToOnboardSDKDevice(sendData_str.getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });

                    fragmentChange();
                }
            }
        });

        mAverage2_edt = (EditText) view.findViewById(R.id.double_threshold_average2_editText);
        mThreshold1_2_edt = (EditText) view.findViewById(R.id.double_threshold_threshold1_2_editText);
        mThreshold2_2_edt = (EditText) view.findViewById(R.id.double_threshold_threshold2_2_editText);
        mSetting2_btn = (Button) view.findViewById(R.id.double_threshold_setting2_button);
        mSetting2_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    mSetting2_btn.setEnabled(false);

                    String average_str = mAverage1_edt.getText().toString();
                    String threshold1_str = mThreshold1_1_edt.getText().toString();
                    String threshold2_str = mThreshold2_1_edt.getText().toString();

                    String average2_str = mAverage2_edt.getText().toString();
                    String threshold1_2_str = mThreshold1_2_edt.getText().toString();
                    String threshold2_2_str = mThreshold2_2_edt.getText().toString();
                    String sendData_str = "configure:" + average_str + "," + threshold1_str + "," + threshold2_str + "," + average2_str + "," + threshold1_2_str + "," + threshold2_2_str + "\0";

                    mFlightController.sendDataToOnboardSDKDevice(sendData_str.getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });

                    fragmentChange();
                }
            }
        });

        initFlightController();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
            mParent.showToast(data);
        }
    };

    private void fragmentChange() {
        FragmentTransaction transaction = mParent.getSupportFragmentManager().beginTransaction();
        MarginInputFragment nextFragment = new MarginInputFragment();
        transaction.replace(R.id.fragment_container, nextFragment, MarginInputFragment.class.getName());
        transaction.addToBackStack(MarginInputFragment.class.getName());
        transaction.commit();
    }
}
