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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

public class VisualizationFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    
    private JMenuBar menuBar;
    private RobotRunSurface surface;
    private ControlPane controlPane;
    private Context context;
    
    public VisualizationFrame(Context context) {
        super("Kaa Robot Run");
        this.context = context;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
        initMenu();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize(screenSize.width/2, screenSize.height/2);
        setLocationRelativeTo ( null );
        setVisible ( true );
    }
    
    private void initUI() {
        this.setLayout(new BorderLayout());
        surface = new RobotRunSurface(context, context.getLabyrinth(), context.getRobotPositions(), true, false);
        context.registerRobotRunListener(surface);
        controlPane = new ControlPane(context);
        context.registerContextListener(controlPane);
        add(surface, BorderLayout.CENTER);
        add(controlPane, BorderLayout.EAST);
        
        WindowAdapter adapter = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                context.unregisterRobotRunListener(surface);
                context.unregisterContextListener(controlPane);
            }
        };
        addWindowListener(adapter);
    }
    
    private void initMenu() {
        JMenu menu;
        JMenuItem menuItem;
        menuBar = new JMenuBar();
        menu = new JMenu("Main");
        
        menuBar.add(menu);

        menu.addSeparator();
        
        menuItem = new JMenuItem("Exit");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(menuItem);
        
        setJMenuBar(menuBar);
    }
    

}
