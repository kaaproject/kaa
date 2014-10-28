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

package org.kaaproject.kaa.examples.robotrun.visualization;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.kaaproject.kaa.examples.robotrun.visualization.Context.ContextListener;
import org.kaaproject.kaa.examples.robotrun.visualization.LabyrinthDialog.LabyrinthDialogListener;

public class ControlPane extends JPanel implements ContextListener,
                                                   LabyrinthDialogListener {

    private static final long serialVersionUID = 1L;
    private JTextField widthField;
    private JTextField heightField;
    private Context context;
    
    private JButton btnCreateNewLabirynth;
    private JButton btnResetCurrentLabirynth;
    private JButton btnStartRun;
    private JButton btnStopRun;
    private JButton btnSetupEmulator;
    private JButton btnStopEmulator;
    private JButton btnShowEmulatorLabyrinth;
    
    public ControlPane(Context context) {
        this.context = context;
        
        setLayout(new BorderLayout(0, 0));
        
        JPanel panel_4 = new JPanel();
        add(panel_4, BorderLayout.NORTH);
        panel_4.setLayout(new GridLayout(0, 1, 0, 0));
        
        JPanel panel = new JPanel();
        panel_4.add(panel);
        panel.setBorder(new TitledBorder(null, "Maze", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.setLayout(new BorderLayout(10, 0));
        
        JPanel panel_1 = new JPanel();
        panel.add(panel_1, BorderLayout.WEST);
        panel_1.setLayout(new GridLayout(0, 1, 0, 0));
        
        JLabel lblWidth = new JLabel("Width:");
        panel_1.add(lblWidth);
        
        JLabel lblHeight = new JLabel("Height:");
        panel_1.add(lblHeight);
        
        JPanel panel_2 = new JPanel();
        panel.add(panel_2, BorderLayout.CENTER);
        panel_2.setLayout(new GridLayout(0, 1, 0, 10));
        
        widthField = new JTextField();
        widthField.setEditable(false);
        panel_2.add(widthField);
        widthField.setColumns(10);
        
        heightField = new JTextField();
        heightField.setEditable(false);
        panel_2.add(heightField);
        heightField.setColumns(10);
        
        JPanel panel_3 = new JPanel();
        panel_4.add(panel_3);
        panel_3.setBorder(new TitledBorder(null, "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_3.setLayout(new GridLayout(0, 1, 0, 0));
        
        btnCreateNewLabirynth = new JButton("Create new maze");
        btnCreateNewLabirynth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createLabirynth();
            }
        });

        panel_3.add(btnCreateNewLabirynth);
        
        btnResetCurrentLabirynth = new JButton("Reset current maze");
        btnResetCurrentLabirynth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetLabyrinth();
            }
        });
        
        panel_3.add(btnResetCurrentLabirynth);
        
        btnStartRun = new JButton("Start Run!");
        btnStartRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //testLabyrinthUpdate();
                startRun();
            }
        });
        
        
        panel_3.add(btnStartRun);
        
        btnStopRun = new JButton("Stop Run!");
        btnStopRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //testLabyrinthUpdate();
                stopRun();
            }
        });
        btnStopRun.setEnabled(false);
        
        panel_3.add(btnStopRun);
        
        btnSetupEmulator = new JButton("Setup emulator");
        btnSetupEmulator.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setupEmulator();
            }
        });
        panel_3.add(btnSetupEmulator);

        btnStopEmulator = new JButton("Stop emulator");
        btnStopEmulator.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopEmulator();
            }
        });
        btnStopEmulator.setEnabled(false);
        panel_3.add(btnStopEmulator);
        
        btnShowEmulatorLabyrinth = new JButton("Show Emulator Maze");
        btnShowEmulatorLabyrinth.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showEmulatorLabyrinth();
            }
        });
        panel_3.add(btnShowEmulatorLabyrinth);
        
        updateData(context.getLabyrinth());
        
    }
    
    private void updateData(Labyrinth data) {
        widthField.setText(""+data.getWidth());
        heightField.setText(""+data.getHeight());
    }

    @Override
    public void onLabyrinthUpdated(Labyrinth data) {
        updateData(data);
    }
    
    private void createLabirynth() {
        JTextField widthField = new JTextField();
        JTextField heightField = new JTextField();
        final JComponent[] inputs = new JComponent[] {
                new JLabel("Width"),
                widthField,
                new JLabel("Height"),
                heightField
        };
        JOptionPane.showMessageDialog(null, inputs, "Create maze", JOptionPane.PLAIN_MESSAGE);
        int width = 0;
        int height = 0;
        
        try {
            width = Integer.valueOf(widthField.getText());
            height = Integer.valueOf(heightField.getText());
        }
        catch (Exception e) {};
        if (width > 0 && height > 0) {
            context.createLabirynth(width, height);
        }
        else {
            Context.showErrorDialog("Invalid width/height parameters!", false);
        }
    }
    
    private void resetLabyrinth() {
        context.resetLabyrinth();
    }
    
    private void startRun() {
        context.startRun();
    }
    
    private void stopRun() {
        context.stopRun();
    }
    
    private void testLabyrinthUpdate() {
        Thread t = new Thread(
            new Runnable () {
                @Override
                public void run() {
                    Labyrinth labyrinth = context.getLabyrinth();
                    for (int y=0;y<labyrinth.getHeight();y++) {
                        for (int x=0;x<labyrinth.getWidth();x++) {
                            for (Direction side : Direction.values()) {
                                context.updateCell(x, y, side, BorderType.SOLID);
                                //try {
                                //    Thread.sleep(500);
                               // } catch (InterruptedException e) {}
                            }
                        }
                    }
                }
            }, 
           "Maze updater");
        t.start();
    }
    
    private void setupEmulator() {
        context.showEmulatorSetupDialog(null);
    }
    
    private void stopEmulator() {
        context.stopEmulator();
    }
    
    private void showEmulatorLabyrinth() {
        context.showEmulatedLabyrinthDialog(null, this);
    }

    @Override
    public void onEmulatorSetupComplete() {
        btnSetupEmulator.setEnabled(false);
        btnStopEmulator.setEnabled(true);
        btnStartRun.setEnabled(false);
    }

    @Override
    public void onEmulatorInitCompleted() {
        btnStartRun.setEnabled(true);
    }

    @Override
    public void onLabyrinthDialogShow() {
    }

    @Override
    public void onLabyrinthDialogHide() {
    }

    @Override
    public void onEmulatorStopped() {
        btnSetupEmulator.setEnabled(true);
        btnStopEmulator.setEnabled(false);
    }

    @Override
    public void onRobotLocationChanged(Map<String, RobotPosition> robotPositions) {
        // do nothing
        
    }

    @Override
    public void onStartRun() {
        btnCreateNewLabirynth.setEnabled(false);
        btnResetCurrentLabirynth.setEnabled(false);
        btnStartRun.setEnabled(false);
        btnStopRun.setEnabled(true);
        btnSetupEmulator.setEnabled(false);
    }

    @Override
    public void onExitFoundCompleted() {
        btnCreateNewLabirynth.setEnabled(true);
        btnResetCurrentLabirynth.setEnabled(true);
        btnStartRun.setEnabled(true);
        btnStopRun.setEnabled(false);
        btnSetupEmulator.setEnabled(!context.isEmulatorStarted());
    }

}
