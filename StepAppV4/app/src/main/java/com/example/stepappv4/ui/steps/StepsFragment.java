package com.example.stepappv4.ui.steps;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.stepappv4.R;
import com.example.stepappv4.StepCounterListener;
import com.example.stepappv4.databinding.FragmentStepsBinding;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.example.stepappv4.StepAppOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class StepsFragment extends Fragment {

    private FragmentStepsBinding binding;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private TextView stepsTextView;
    private CircularProgressIndicator progressBar;

    private int stepsCounter = 0;
    private Sensor detectorSensor;
    private SensorManager sensorManager;

    private StepCounterListener sensorListener;

    public StepAppOpenHelper stepAppOpenHelper;
    private int goal = 1000;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StepsViewModel homeViewModel =
                new ViewModelProvider(this).get(StepsViewModel.class);

        binding = FragmentStepsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // set goal
        TextView goalTextview =  (TextView) root.findViewById(R.id.goal_textview);
        goalTextview.setText(String.format(Locale.getDefault(),"Goal: %d", goal));

        //Timestamp
        long timeInMillis = System.currentTimeMillis();
        // Convert the timestamp to date
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        jdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        final String dateTimestamp = jdf.format(timeInMillis);
        String currentDay = dateTimestamp.substring(0,10);

        stepAppOpenHelper = new StepAppOpenHelper(getContext());
        stepsCounter = stepAppOpenHelper.loadSingleRecord(getContext(), currentDay);
        Log.d("RETRIEVED STEPS TODAY: ", String.valueOf(stepsCounter));

        progressBar = (CircularProgressIndicator)  root.findViewById(R.id.progressBar);
        progressBar.setMax(goal);
        progressBar.setProgress(stepsCounter);

        stepsTextView = (TextView) root.findViewById(R.id.stepsCount_textview);
        stepsTextView.setText(""+stepsCounter);

        // TODO 3: Get an instance of sensor manager
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        // detectorSensor
        detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Toggle group button
        materialButtonToggleGroup = (MaterialButtonToggleGroup) root.findViewById(R.id.toggleButtonGroup);
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked){
                    handleButtonCheck(checkedId);
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void handleButtonCheck(int checkedId) {
        if (checkedId == R.id.toggleStart_btn) {
            if (detectorSensor != null) {
                sensorListener = new StepCounterListener(getContext(), stepsTextView, this.progressBar, stepsCounter);

                sensorManager.registerListener(sensorListener, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
                Toast.makeText(getContext(), R.string.start_text, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), R.string.acc_sensor_not_available, Toast.LENGTH_LONG).show();
            }
        } else if (checkedId == R.id.toggleStop_btn) {
            sensorListener.stopRecording();
            sensorManager.unregisterListener(sensorListener);
            Toast.makeText(getContext(), R.string.stop_text, Toast.LENGTH_LONG).show();
        }
    }
}