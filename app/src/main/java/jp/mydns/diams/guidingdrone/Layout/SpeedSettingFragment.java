package jp.mydns.diams.guidingdrone.Layout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.UnsupportedEncodingException;

import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import jp.mydns.diams.guidingdrone.Common.GuidingDroneApplication;
import jp.mydns.diams.guidingdrone.R;

public class SpeedSettingFragment extends Fragment {

    private View view;
    private MainActivity mParent;

    private RadioGroup mRadioGroup;
    private Button mSetting_btn;

    private FlightController mFlightController;

    public static SpeedSettingFragment newInstance(String param1, String param2) {
        SpeedSettingFragment fragment = new SpeedSettingFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_speed_setting, container, false);
        mParent = (MainActivity) getActivity();

        mRadioGroup = (RadioGroup) view.findViewById(R.id.speed_setting_radioGroup);

        mSetting_btn = (Button) view.findViewById(R.id.speed_setting_setting_button);
        mSetting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFlightController != null) {
                    String sendMes = "";
                    if (mRadioGroup.getCheckedRadioButtonId() == R.id.speed_setting_fast_radioButton) {
                        sendMes = "speed:1.0\0";
                    } else if (mRadioGroup.getCheckedRadioButtonId() == R.id.speed_setting_slow_radioButton) {
                        sendMes = "speed:0.7\0";
                    }

                    if (!sendMes.equals("")) {
                        mFlightController.sendDataToOnboardSDKDevice(sendMes.getBytes(), new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                            }
                        });
                        fragmentChange();
                    } else {
                        mParent.showToast("ちゃんと速度を設定して");
                    }
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
        RssiMeasurement nextFragment = new RssiMeasurement();
        transaction.replace(R.id.fragment_container, nextFragment, RssiMeasurement.class.getName());
        transaction.addToBackStack(RssiMeasurement.class.getName());
        transaction.commit();
    }
}
