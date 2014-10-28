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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;

public class LabyrinthDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    public static LabyrinthDialog showLabyrinthDialog(Component parent,
            Labyrinth labyrinth, LabyrinthDialogListener listener) {
     
        LabyrinthDialog dialog = new LabyrinthDialog(labyrinth,
                listener);
        dialog.setTitle("Maze");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(screenSize.width / 3, screenSize.width / 3);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return dialog;
    }
    
    private LabyrinthDialogListener listener;
    private LabyrinthSurface surfacePanel;
    
    private LabyrinthDialog(Labyrinth labyrinth, LabyrinthDialogListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        surfacePanel = new LabyrinthSurface(labyrinth);
        add(surfacePanel, BorderLayout.CENTER);
        WindowAdapter adapter = new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                LabyrinthDialog.this.listener.onLabyrinthDialogShow();
            }
            
            public void windowClosing(WindowEvent e) {
                LabyrinthDialog.this.listener.onLabyrinthDialogHide();
            }
        };
        addWindowListener(adapter);
    }
    
    public static interface LabyrinthDialogListener {
        
        void onLabyrinthDialogShow();
        
        void onLabyrinthDialogHide();
        
    }
}
