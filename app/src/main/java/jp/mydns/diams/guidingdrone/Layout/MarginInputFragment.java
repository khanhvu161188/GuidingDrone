package jp.mydns.diams.guidingdrone.Layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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


public class MarginInputFragment extends Fragment {

    private View view;
    private MainActivity mParent;

    private EditText mMarginWeek_edt, mMarginStrong_edt;
    private Button mTransmit_btn;

    private EditText mTurnCount_edt;
    private Button mTurnCount_btn;

    private Button mNextPage_btn;

    private FlightController mFlightController;

    private boolean mTransmit_bl;
    private boolean mTurnCount_bl;

    public MarginInputFragment() {
        // Required empty public constructor
    }

    public static MarginInputFragment newInstance(String param1, String param2) {
        MarginInputFragment fragment = new MarginInputFragment();
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
        view = inflater.inflate(R.layout.fragment_margin_input, container, false);
        mParent = (MainActivity) getActivity();

        mMarginWeek_edt = (EditText) view.findViewById(R.id.margin_input_margin_editText);
        mMarginStrong_edt = (EditText) view.findViewById(R.id.margin_input_margin_strong_editText);

        mTransmit_btn = (Button) view.findViewById(R.id.margin_input_transmit_button);
        mTransmit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    String sendData_str = "margin:" + mMarginWeek_edt.getText().toString() + ":" + mMarginStrong_edt.getText().toString() + "\0";

                    mFlightController.sendDataToOnboardSDKDevice(sendData_str.getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
                    mTransmit_btn.setEnabled(false);
                    mTransmit_bl = true;
                }
            }
        });

        mTurnCount_edt = (EditText) view.findViewById(R.id.margin_input_turn_count_editText);
        mTurnCount_btn = (Button) view.findViewById(R.id.margin_input_turn_count_button);
        mTurnCount_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    String sendData_str = "turnCnt:" + mTurnCount_edt.getText().toString() + "\0";

                    mFlightController.sendDataToOnboardSDKDevice(sendData_str.getBytes(), new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {

                        }
                    });
                    mTurnCount_btn.setEnabled(false);
                    mTurnCount_bl = true;
                }
            }
        });

        mNextPage_btn = (Button) view.findViewById(R.id.margin_input_next_page_button);
        mNextPage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTransmit_bl && mTurnCount_bl) {
                    fragmentChange();
                }
            }
        });

        mTransmit_bl = false;
        mTurnCount_bl = false;

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
        SimpleOSDKFragment nextFragment = new SimpleOSDKFragment();
        transaction.replace(R.id.fragment_container, nextFragment, SimpleOSDKFragment.class.getName());
        transaction.addToBackStack(SimpleOSDKFragment.class.getName());
        transaction.commit();
    }
}
