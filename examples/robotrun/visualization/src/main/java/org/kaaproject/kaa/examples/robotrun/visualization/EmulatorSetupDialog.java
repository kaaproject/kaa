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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.impl.BasicLabyrinthGenerator;
import org.kaaproject.kaa.examples.robotrun.visualization.emulator.EmulatorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmulatorSetupDialog extends JDialog {

    private static final long serialVersionUID = 5927779329048923400L;

    private static final Logger LOG = LoggerFactory
            .getLogger(EmulatorSetupDialog.class);

    private JButton okButton;
    private LabyrinthSurface surfacePanel;
    private JLabel statusLabel;
    private JButton generateLabyrinthButton;
    private JButton placeRobotsButton;
    private JButton resetButton;


    public static EmulatorSetupDialog showEmulatorSetupDialog(Component parent,
            Context context, EmulatorSetupDialogListener listener) {
        EmulatorSetupDialog dialog = new EmulatorSetupDialog(context,
                listener);
        dialog.setModal(true);
        dialog.setTitle("Setup Robot Run Emulator");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setSize(screenSize.width / 3, screenSize.width / 3);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return dialog;
    }

    private Map<Cell, Direction> robots;
    private Cell startCell;
    private Cell finishCell;
    private Labyrinth labyrinth;
    private EmulatorSetupDialogListener listener;
    private Context context;
    
    private boolean labyrinthGenerated = false;

    
    private EmulatorSetupDialog(final Context context,
            EmulatorSetupDialogListener listener) {
        this.robots = new HashMap<>(context.getEmulatorRobots());
        this.context = context;
        Labyrinth saved = EmulatorManager.loadLabyrinth();
        if (saved != null) {
            this.labyrinth = saved;
            labyrinthGenerated = true;
        }
        else {
            this.labyrinth = new BasicLabyrinth(context.getLabyrinth().getWidth(), context.getLabyrinth().getHeight());
        }
        
        this.listener = listener;

        setLayout(new BorderLayout());
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel();
        statusPanel.add(statusLabel);
        generateLabyrinthButton = new JButton("Generate maze");
        generateLabyrinthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateLabyrinth();
            }
        });

        placeRobotsButton = new JButton("Place robots");
        placeRobotsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeRobots();
            }
        });
        
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset(true);
            }
        });
        
        statusPanel.add(generateLabyrinthButton);
        statusPanel.add(placeRobotsButton);
        statusPanel.add(resetButton);
        
        add(statusPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("Start Emulator");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EmulatorSetupDialog.this.dispose();
                EmulatorSetupDialog.this.listener.onEmulatorSetupDialogOk(robots);
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EmulatorSetupDialog.this.dispose();
                EmulatorSetupDialog.this.listener.onEmulatorSetupDialogCancel();
            }
        });

        buttonPanel.add(okButton);
        buttonPanel.add(closeButton);

        surfacePanel = new EmulatorSetupSurface(labyrinth);
        add(surfacePanel, BorderLayout.CENTER);
        
        if (!robots.isEmpty()) {
            placeRobots();
            okButton.setEnabled(true);
        }
        else {
            reset(false);
        }
    }
    
    private void placeRobots() {
        labyrinthGenerated = true;
        surfacePanel.repaint();
        generateLabyrinthButton.setEnabled(false);
        placeRobotsButton.setEnabled(false);
        statusLabel.setText("Place robots inside cells");
    }
    
    private void reset(boolean resetLabyrinth) {
        if (resetLabyrinth || !labyrinthGenerated) {
            this.labyrinth = new BasicLabyrinth(context.getLabyrinth().getWidth(), context.getLabyrinth().getHeight());
            labyrinthGenerated = false;
            startCell = null;
            finishCell = null;
            statusLabel.setText("Select start and finish cells");
        }
        else {
            placeRobots();
        }
        robots.clear();
        surfacePanel.onLabyrinthUpdated(EmulatorSetupDialog.this.labyrinth);
        surfacePanel.repaint();
        generateLabyrinthButton.setEnabled(false);
        placeRobotsButton.setEnabled(false);
        okButton.setEnabled(false);
    }
    
    private void generateLabyrinth() {
        int startX = startCell.getX();
        int startY = startCell.getY();
        int finishX = finishCell.getX();
        int finishY = finishCell.getY();
        labyrinth = new BasicLabyrinthGenerator(labyrinth.getWidth(), labyrinth.getHeight()).generate(startX, startY, finishX, finishY);
        surfacePanel.onLabyrinthUpdated(labyrinth);
        surfacePanel.repaint();
        placeRobotsButton.setEnabled(true);
        EmulatorManager.saveLabyrinth(labyrinth);
    }

    private Object showInputDialog(int x, int y, 
            Object[] possibilities, 
            Object initialValue,
            String message, String title) {
        final JOptionPane pane = new JOptionPane(message,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
                null, null, null);
        pane.setWantsInput(true);
        pane.setSelectionValues(possibilities);
        pane.setInitialSelectionValue(initialValue);
        
        final JDialog dialog = new JDialog(this, title, true);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocation(x-dialog.getWidth()/2, y-dialog.getHeight()/2);
        final PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (dialog.isVisible()
                        && event.getSource() == pane
                        && (event.getPropertyName()
                                .equals(JOptionPane.VALUE_PROPERTY))
                        && event.getNewValue() != null
                        && event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    dialog.setVisible(false);
                }
            }
        };

        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            public void windowClosing(WindowEvent we) {
                pane.setValue(null);
            }

            public void windowClosed(WindowEvent e) {
                removePropertyChangeListener(listener);
                dialog.getContentPane().removeAll();
            }

            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    pane.selectInitialValue();
                    gotFocus = true;
                }
            }
        };
        dialog.addWindowListener(adapter);
        dialog.addWindowFocusListener(adapter);
        dialog.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                // reset value to ensure closing works properly
                pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
        });

        pane.addPropertyChangeListener(listener);

        pane.selectInitialValue();
        dialog.show();
        dialog.dispose();

        Object value = pane.getInputValue();
        if (value == JOptionPane.UNINITIALIZED_VALUE) {
            value = null;
        }
        return value;
    }

    void onCellClicked(int x, int y, int col, int row) {
        Cell cell = labyrinth.getCell(col, row);
        if (!labyrinthGenerated) {
            Object[] possibilities = new String[]{"Start", "Finish"};
            int index = startCell != null ? 1 : 0;
            String type = (String) showInputDialog(x,y,possibilities, possibilities[index], "Specify cell type", "Generate maze");
            if (type != null) {
                if ("Start".equals(type)) {
                    if (cell.equals(finishCell)) {
                        finishCell = null;
                    }
                    startCell = cell;
                }
                else {
                    if (cell.equals(startCell)) {
                        startCell = null;
                    }
                    finishCell = cell;
                }
            }
            generateLabyrinthButton.setEnabled(startCell != null && finishCell != null);
        } 
        else {
            if (!robots.containsKey(cell)) {
                Object[] possibilities = Direction.values();
                Direction direction = (Direction) showInputDialog(x,y,possibilities, possibilities[0], "Specify direction", "Robot direction");
                if (direction != null) {
                    EmulatorSetupDialog.this.robots.put(cell, direction);
                }
            } else {
                robots.remove(cell);
            }
        }
        okButton.setEnabled(!robots.isEmpty());
        surfacePanel.repaint();
    }
    
    private static final Font textFont = new Font("Dialog", Font.BOLD, 16);
    private static final Color startCellColor = new Color(212,246,140);
    private static final Color startCellTextColor = new Color(88,109,110);
    private static final Color finishCellColor = new Color(246,205,140);
    private static final Color finishCellTextColor = new Color(112,108,101);
    private static final Color robotCellColor = new Color(120,156,157);
    private static final Color robotCellTextColor = Color.white;

    class EmulatorSetupSurface extends LabyrinthSurface {       

        private static final long serialVersionUID = 5521959683144844818L;

        public EmulatorSetupSurface(Labyrinth labyrinth) {
            super(labyrinth);
            this.addMouseListener(new MouseListener() {

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    processMouseClick(e.getXOnScreen(), e.getYOnScreen(), x, y);
                }
            });
        }

        private void processMouseClick(int absX, int absY, int x, int y) {
            if (x > offsetX && x < getWidth() - offsetX && y > offsetY
                    && y < getHeight() - offsetY) {

                int col = (x - offsetX) / side;
                int row = (y - offsetY) / side;
                onCellClicked(absX, absY, col, row);
            }
        }

        @Override
        protected void doDrawing(Graphics2D g2d) {
            if (!labyrinthGenerated) {
                if (startCell != null) {
                    drawCell(g2d, startCell, startCellColor, startCellTextColor, "Start");
                }
                if (finishCell != null) {
                    drawCell(g2d, finishCell, finishCellColor, finishCellTextColor, "Finish");
                }
            }
            else {
                for (Cell cell : robots.keySet()) {
                    int x = cell.getX();
                    int y = cell.getY();
                    Direction dir = robots.get(cell);
                    String text = "[" + x + "," + y + "] "
                            + getDirrectionArrow(dir);
                    drawCell(g2d, cell, robotCellColor, robotCellTextColor, text);
                }
            }
            super.doDrawing(g2d);
        }
        
        private void drawCell(Graphics2D g2d, Cell cell, Color cellColor, Color textColor, String text) {
            int x = cell.getX();
            int y = cell.getY();
            int startX = offsetX + x * side;
            int startY = offsetY + y * side;
            g2d.setColor(cellColor);
            g2d.fillRect(startX, startY, side, side);
            g2d.setColor(textColor);
            g2d.setFont(textFont);
            FontMetrics fm = g2d.getFontMetrics();
            int fontSize = textFont.getSize();
            while (fm.stringWidth(text)+5>side) {
                fontSize--;
                g2d.setFont(new Font("Dialog", Font.BOLD, fontSize));
                fm = g2d.getFontMetrics();
            }
            int textX = startX + (side - fm.stringWidth(text)) / 2;
            int textY=startY+side/2 - (fm.getAscent() + fm.getDescent())/2 + fm.getAscent();
            g2d.drawString(text, textX, textY);
        }

        private String getDirrectionArrow(Direction direction) {
            String directionArrow = "";
            switch (direction) {
            case WEST:
                directionArrow = "\u2190";
                break;
            case NORTH:
                directionArrow = "\u2191";
                break;
            case EAST:
                directionArrow = "\u2192";
                break;
            case SOUTH:
                directionArrow = "\u2193";
                break;
            }
            return directionArrow;
        }
    }

    public static interface EmulatorSetupDialogListener {

        void onEmulatorSetupDialogOk(Map<Cell, Direction> robots);

        void onEmulatorSetupDialogCancel();

    }
}
