/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.examples.robotrun.android;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.kaaproject.kaa.examples.robotrun.controller.Launcher;
import org.kaaproject.kaa.examples.robotrun.controller.Launcher.LauncherCallback;
import org.kaaproject.kaa.examples.robotrun.emulator.RobotEmulator;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class RobotRunFragment extends Fragment implements TextWatcher, LauncherCallback {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(RobotRunFragment.class);
    
    private RobotRunActivity mActivity;
    
    private EditText xInput;
    private EditText yInput;
    private Spinner directionInput;
    private Button startButton;
    private Button startEmulatorButton;
    private Button stopButton;

    private View statusView;
    private TextView statusText;
    private TextView connectedToText;

    private Launcher launcher;
    
    private RobotType robotType = null;
    
    private String connectedEntityName;
    
    public static RobotRunFragment newInstance() {
        RobotRunFragment fragment = new RobotRunFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (RobotRunActivity)this.getActivity();
    }
    
    @Override
    public void onDestroy() {
        mActivity = null;
        super.onDestroy();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.robotrun_fragment, container, false);
        xInput = (EditText) view.findViewById(R.id.x_input);
        xInput.setFilters(new InputFilterMinMax[] { new InputFilterMinMax(0, 12) });
        yInput = (EditText) view.findViewById(R.id.y_input);
        yInput.setFilters(new InputFilterMinMax[] { new InputFilterMinMax(0, 12) });
        directionInput = (Spinner) view.findViewById(R.id.direction_input);

        CharSequence[] directionEntries = new CharSequence[Direction.values().length];
        for (int i = 0; i < Direction.values().length; i++) {
            directionEntries[i] = Direction.values()[i].name();
        }
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getActivity(), android.R.layout.simple_spinner_item, directionEntries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        directionInput.setAdapter(adapter);

        startButton = (Button) view.findViewById(R.id.start_button);
        startEmulatorButton = (Button) view.findViewById(R.id.start_emulator_button);
        stopButton = (Button) view.findViewById(R.id.stop_button);

        statusView = (View) view.findViewById(R.id.status);
        statusText = (TextView) view.findViewById(R.id.status_text);
        connectedToText = (TextView) view.findViewById(R.id.connected_to_text);
        
        startButton.setEnabled(false);
        startEmulatorButton.setEnabled(false);
        stopButton.setEnabled(false);

        xInput.addTextChangedListener(this);
        yInput.addTextChangedListener(this);
        
        xInput.setText("0");
        yInput.setText("0");

        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        startEmulatorButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startEmulator();
            }
        });

        stopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        
        return view;
    }
    
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkValues();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
    
    private void checkValues() {
        CharSequence xText = xInput.getText();
        CharSequence yText = yInput.getText();
        if (!isEmpty(xText) && !isEmpty(yText)) {
            startButton.setEnabled(true);
            startEmulatorButton.setEnabled(true);
        } else {
            startButton.setEnabled(false);
            startEmulatorButton.setEnabled(false);
        }
    }
    
    private static boolean isEmpty(CharSequence chs) {
        return chs == null || chs.length() == 0;
    }
    
    private int getStartX() {
        CharSequence xText = xInput.getText();
        return Integer.valueOf(xText.toString());
    }
    
    private int getStartY() {
        CharSequence yText = yInput.getText();
        return Integer.valueOf(yText.toString());
    }
    
    private Direction getStartDirection() {
        CharSequence dirString = (CharSequence)directionInput.getSelectedItem();
        return Direction.valueOf(dirString.toString());
    }

    private void disableInput() {
        xInput.setEnabled(false);
        yInput.setEnabled(false);
        directionInput.setEnabled(false);
        startButton.setEnabled(false);
        startEmulatorButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void enableInput() {
        xInput.setEnabled(true);
        yInput.setEnabled(true);
        directionInput.setEnabled(true);
        startButton.setEnabled(true);
        startEmulatorButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void start() {
        updateStatus(RobotRunStatus.SETTING_UP);
        int startX = getStartX();
        int startY = getStartY();
        Direction startDirection = getStartDirection();
        try {
            launcher = new AndroidBtLauncher(this, getActivity(), startX, startY, startDirection, "Unknown bot");
        } catch (Exception e) {
            LOG.error("Failed to create android bt launcher!",  e);
            showError("Failed to create android bt launcher!\n" + e.getMessage());
            stop();
        }
        if (launcher != null) {
            robotType = RobotType.BOT;
            startLauncher();
        }
    }

    private void startEmulator() {
        updateStatus(RobotRunStatus.SETTING_UP);
        File labyrinthFile = new File(
                Environment.getExternalStorageDirectory(), "labyrinth.data");
        Labyrinth labyrinth = tryReadEmulatorLabyrinth(labyrinthFile);
        if (labyrinth == null) {
            showError("Unable to load labyrinth from file:\n"
                    + labyrinthFile.getAbsolutePath());
            stop();
        } else {
            int startX = getStartX();
            int startY = getStartY();
            Direction startDirection = getStartDirection();
            Cell startCell = labyrinth.getCell(startX, startY);
            Properties robotProperties = new Properties();
            robotProperties.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT,""+ RobotEmulator.DEFAULT_COMMAND_TIMEOUT/5);
            robotProperties.put(RobotEmulator.PROPERTY_NAME_COMMAND_TIMEOUT_DEVIATION,""+ RobotEmulator.DEFAULT_COMMAND_TIMEOUT_DEVIATION/5);
            robotProperties.put(RobotEmulator.PROPERTY_NAME_PING_TIMEOUT,""+ RobotEmulator.DEFAULT_PING_TIMEOUT/5);
            try {
                launcher = new AndroidEmulatorLauncher(this, getActivity(), labyrinth, startCell, startDirection, "android emulated bot", robotProperties);
            } catch (Exception e) {
                LOG.error("Failed to create emulator launcher!",  e);
                showError("Failed to create emulator launcher!\n" + e.getMessage());
                stop();
            }
            if (launcher != null) {
                robotType = RobotType.EMULATOR;
                startLauncher();
            }
        }
    }
    
    private void startLauncher() {
        try {
            launcher.start();
        }
        catch (RuntimeException e) {
            LOG.error("Failed to start launcher!",  e);
            showError("Failed to start launcher!\n" + e.getMessage());
            stop();
        }
    }

    private void stop() {
        if (launcher != null) {
            launcher.stop();
            launcher = null;
        }
        else {
            updateStatus(RobotRunStatus.SETUP);
        }
    }

    @Override
    public void onLauncherStarted(String entityName) {
        connectedEntityName = entityName;
        updateStatus(RobotRunStatus.READY);
    }
    
    @Override
    public void onLauncherStartRun() {
        updateStatus(RobotRunStatus.STARTED);
    }

    @Override
    public void onLauncherStopped() {
        updateStatus(RobotRunStatus.SETUP);
    }
    
    @Override
    public void onLauncherError(final Exception e) {
        if (launcher != null) {
            LOG.error("Launcher error!",  e);
            getActivity().runOnUiThread(new Runnable () {
                @Override
                public void run() {
                    showError("Launcher error!\n" + e.getMessage());
                    stop();
                }
            });
        }
    }

    private void updateStatus(final RobotRunStatus status) {
        getActivity().runOnUiThread(new Runnable () {
            @Override
            public void run() {
                statusView.setBackgroundResource(status.backgroundResId);
                statusText.setText(getResources().getText(status.textResId));
                Animation fadeAnimation;
                switch (status) {
                case SETUP:
                    enableInput();
                    connectedToText.setVisibility(View.GONE);
                    statusView.clearAnimation();
                    break;
                case SETTING_UP:
                    disableInput();
                    fadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_infinite); 
                    statusView.startAnimation(fadeAnimation);
                    break;
                case READY:
                    statusView.clearAnimation();
                    connectedToText.setVisibility(View.VISIBLE);
                    connectedToText.setText(
                            String.format(getResources().getString(R.string.connected_to_message), 
                                    getResources().getString(robotType.textResId),
                                    connectedEntityName));
                    break;
                case STARTED:
                    fadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_infinite); 
                    statusView.startAnimation(fadeAnimation);
                    break;
                    default:
                        break;
                }
            }
            
        });
    }
    
    private Labyrinth tryReadEmulatorLabyrinth(File labyrinthFile) {
        Labyrinth labyrinth = null;
        if (labyrinthFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(labyrinthFile);
                labyrinth = BasicLabyrinth.load(fis);
            } catch (Exception e) {
                LOG.error("Unable to load labyrinth from file [{}]!",
                        labyrinthFile);
            }
        }
        return labyrinth;
    }
    
    private void showError(String message) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setMessage(message)
        .setTitle("Error")
        .setNeutralButton("Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(
                            final DialogInterface dialog,
                            final int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

}
