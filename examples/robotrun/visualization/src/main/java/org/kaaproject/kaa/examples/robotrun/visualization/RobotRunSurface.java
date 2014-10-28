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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.kaaproject.kaa.examples.robotrun.visualization.Context.RobotRunListener;

public class RobotRunSurface extends LabyrinthSurface {

    private static final long serialVersionUID = 1L;
    
    private static final Color robotStrokeColor = Color.white;//new Color(238,248,217);
    private static final Color robotFillColor = new Color(212,246,140);
    private static final Color robotTextColor = Color.white;
    
    private static final Font textFont = new Font("Dialog", Font.BOLD, 16);
    
    private static final BasicStroke ROBOT_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL);
    
    private Map<String, RobotPosition> robotPositions;
    
    private Context context;

    public RobotRunSurface(Context context, 
            Labyrinth labyrinth, 
            Map<String, RobotPosition> robotPositions, 
            boolean mainSurface,
            boolean fullScreen) {
        super(labyrinth);
        this.context = context;
        this.robotPositions = robotPositions;
        if (mainSurface) {
            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e){
                    if (e.isPopupTrigger())
                        doPop(e);
                }

                public void mouseReleased(MouseEvent e){
                    if (e.isPopupTrigger())
                        doPop(e);
                }

                private void doPop(MouseEvent e){
                    ScreenMenu menu = new ScreenMenu();
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            });
        }
        else if (!fullScreen){
            this.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2){
                        RobotRunSurface surface = 
                                new RobotRunSurface(RobotRunSurface.this.context, 
                                        RobotRunSurface.this.labyrinth, 
                                        RobotRunSurface.this.robotPositions, false, true);
                        showFullScreen(RobotRunSurface.this.context, surface, surface, e);
                    }
                }
            });
        }
    }
    
    @Override
    protected void doDrawing(Graphics2D g2d) {
        super.doDrawing(g2d);
        g2d.setStroke(ROBOT_STROKE);
        for (String key : robotPositions.keySet()) {
            RobotPosition position = robotPositions.get(key);
            Cell cell = position.getCell();
            int x = offsetX+cell.getX()*side + side/4;
            int y = offsetY+cell.getY()*side + side/4;
            g2d.setColor(new Color(key.hashCode())/*robotFillColor*/);
            g2d.fillOval(x, y, side/2, side/2);
            g2d.setColor(robotStrokeColor);
            g2d.drawOval(x, y, side/2, side/2);
            String text = position.getName();
            g2d.setColor(robotTextColor);
            g2d.setFont(textFont);
            FontMetrics fm = g2d.getFontMetrics();
            int fontSize = textFont.getSize();
            int textHeight = side/4;
            while (fm.getHeight()>textHeight) {
                fontSize--;
                g2d.setFont(new Font("Dialog", Font.BOLD, fontSize));
                fm = g2d.getFontMetrics();
            }
            while (fm.stringWidth(text)+5>side) {
                fontSize--;
                g2d.setFont(new Font("Dialog", Font.BOLD, fontSize));
                fm = g2d.getFontMetrics();
            }
            int textX = x + (side/2 - fm.stringWidth(text)) / 2;
            //int textY=y+side/4 - (fm.getAscent() + fm.getDescent())/2 + fm.getAscent();
            int textY=offsetY+cell.getY()*side+fm.getHeight();
            g2d.drawString(text, textX, textY);
        }
    }

    @Override
    public void onRobotLocationChanged(Map<String, RobotPosition> robotPositions) {
        this.robotPositions = robotPositions;
        this.repaint();
    }

    private static final KeyStroke escapeStroke = 
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); 
    
    public static final String dispatchWindowClosingActionMapKey = 
            "org.kaaproject.kaa.examples.robotrun.visualization:WINDOW_CLOSING"; 
        
    public static void installEscapeCloseOperation(final JFrame frame, final GraphicsDevice device, final boolean isFullscreen) { 
            Action dispatchClosing = new AbstractAction() {

                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent event) { 
                    if (device != null && isFullscreen) {
                        device.setFullScreenWindow(null);
                    }
                    frame.dispatchEvent(new WindowEvent( 
                            frame, WindowEvent.WINDOW_CLOSING 
                    )); 
                } 
            };
            JRootPane root = frame.getRootPane(); 
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, dispatchWindowClosingActionMapKey); 
            root.getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing);    
    }
    
    private void show2d() {
        RobotRunSurface surface = new RobotRunSurface(context, labyrinth, robotPositions, false, false);
        showScreen(context, surface, surface);
    }
    
    
    private void show3d() {
        RobotRunSurface3d surface = new RobotRunSurface3d(context, labyrinth, robotPositions, false);
        showScreen(context, surface, surface);
    }
    
    private static JFrame prepareFrame(final Context context, Component comp, final RobotRunListener listener) {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        context.registerRobotRunListener(listener);
        
        WindowAdapter adapter = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                context.unregisterRobotRunListener(listener);
            }
        };
        frame.addWindowListener(adapter);

        frame.add(comp, BorderLayout.CENTER);
        
        return frame;
    }
    
    private void showScreen(Context context, Component comp, RobotRunListener listener) {
        
        JFrame frame = prepareFrame(context, comp, listener);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int size = Math.max(screenSize.width/2, screenSize.height/2);
        frame.setSize(size, size);
        frame.setLocationRelativeTo ( null );
        
        frame.setVisible(true);
    }
    
    public static void showFullScreen(Context context, Component comp, RobotRunListener listener,  MouseEvent e) {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        Point point = e.getLocationOnScreen();
        
        GraphicsDevice currentDevice = null;
        
        for (GraphicsDevice device: devices) { 
            GraphicsConfiguration[] configurations =
                device.getConfigurations();
            for (GraphicsConfiguration config: configurations) {
                Rectangle gcBounds = config.getBounds();
                if(gcBounds.contains(point)) {
                    currentDevice = device;
                    break;
                }
            }
            if (currentDevice != null) {
                break;
            }
        }
        
        if(currentDevice == null) {
            currentDevice = env.getDefaultScreenDevice();
        }
        
        boolean isFullScreen = currentDevice != null && currentDevice.isFullScreenSupported();
        
        JFrame frame = prepareFrame(context, comp, listener);
        
        frame.setUndecorated(isFullScreen);
        frame.setResizable(!isFullScreen);
        installEscapeCloseOperation(frame, currentDevice, isFullScreen); 
        
        if (isFullScreen) {
            currentDevice.setFullScreenWindow(frame);
            frame.validate();
        }
        else {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
            frame.setVisible(true);
        }
    }

    class ScreenMenu extends JPopupMenu {
        JMenuItem anItem;
        public ScreenMenu() {
            anItem = new JMenuItem(new AbstractAction("Open 2D Screen") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    show2d();
                }
            });
            add(anItem);
            anItem = new JMenuItem(new AbstractAction("Open 3D Screen") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    show3d();
                }
            });
            add(anItem);
        }
    }

}

