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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Map;

import javax.swing.JPanel;

import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.kaaproject.kaa.examples.robotrun.visualization.Context.RobotRunListener;

public class LabyrinthSurface extends JPanel implements RobotRunListener  {

    private static final long serialVersionUID = 1L;
    
    protected Labyrinth labyrinth;
    
    protected static final int BORDER_SIZE = 20;
    
    private static final Color TRANSPARENT = new Color(0,0,0,0);
    
    private static final Color UNKNOWN_BORDER_COLOR = Color.white;
    private static final Color SOLID_BORDER_COLOR = Color.yellow;
    private static final Color DEAD_END_COLOR = Color.gray;
    
    private static final BasicStroke UNKNOWN_BORDER_STROKE = new BasicStroke(2, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 1.0f, new float[]{4f, 0f, 2f}, 2f);
    private static final BasicStroke SOLID_BORDER_STROKE = new BasicStroke(4, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL);
    
    protected int offsetX, offsetY, side;
    
    public LabyrinthSurface(Labyrinth labyrinth) {
        super();
        this.labyrinth = labyrinth;
    }
    
    protected void doDrawing(Graphics2D g2d) {
        if (!labyrinth.isEmpty()) {
            int labWidth = labyrinth.getWidth();
            int labHeight = labyrinth.getHeight();
            for (int y=0;y<labHeight;y++) {
                for (int x=0;x<labWidth;x++) {
                    Cell cell = labyrinth.getCell(x, y);
                    if(cell.isDeadEnd()){
                        g2d.setColor(DEAD_END_COLOR);
                        g2d.fillRect(offsetX+x*side, offsetY+y*side, side, side);
                    }
                    drawBorder(g2d, offsetX, offsetY, side, x, y, cell, Direction.NORTH);
                    drawBorder(g2d, offsetX, offsetY, side, x, y, cell, Direction.WEST);
                    if (y==labHeight-1) {
                        drawBorder(g2d, offsetX, offsetY, side, x, y, cell, Direction.SOUTH);
                    }
                    if (x==labWidth-1) {
                        drawBorder(g2d, offsetX, offsetY, side, x, y, cell, Direction.EAST);
                    }
                }
            }
            
        }
    }
     
    private void drawBorder(Graphics2D g2d,
            int offsetX, 
            int offsetY, 
            int side, 
            int x, 
            int y, 
            Cell cell, 
            Direction direction) {
        
        BorderType type = cell.getBorder(direction);
        if (type != BorderType.FREE) {
            g2d.setColor(getColor(type));
            g2d.setStroke(getStroke(type));
            switch (direction) {
            case NORTH:
                g2d.drawLine(offsetX+x*side, offsetY+y*side, offsetX+(x+1)*side, offsetY+y*side);
                break;
            case WEST:
                g2d.drawLine(offsetX+x*side, offsetY+y*side, offsetX+x*side, offsetY+(y+1)*side);
                break;
            case SOUTH:
                g2d.drawLine(offsetX+x*side, offsetY+(y+1)*side, offsetX+(x+1)*side, offsetY+(y+1)*side);
                break;
            case EAST:
                g2d.drawLine(offsetX+(x+1)*side, offsetY+y*side, offsetX+(x+1)*side, offsetY+(y+1)*side);
                break;
            }
        }
    }
    
    private BasicStroke getStroke(BorderType type) {
        switch (type) {
        case UNKNOWN:
            return UNKNOWN_BORDER_STROKE;
        case SOLID:
            return SOLID_BORDER_STROKE;
            default:
                return UNKNOWN_BORDER_STROKE;
        }
    }
    
    private Color getColor(BorderType type) {
        switch (type) {
        case UNKNOWN:
            return UNKNOWN_BORDER_COLOR;
        case SOLID:
            return SOLID_BORDER_COLOR;
            default:
                return TRANSPARENT;
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!labyrinth.isEmpty()) {
            
            Dimension size = getSize();
            Insets insets = getInsets();

            int w = size.width - insets.left - insets.right;
            int h = size.height - insets.top - insets.bottom;
            
            int labWidth = labyrinth.getWidth();
            int labHeight = labyrinth.getHeight();
            
            side = Math.min((w-BORDER_SIZE*2)/labWidth, (h-BORDER_SIZE*2)/labHeight);
            int hSize = side*labWidth;
            int vSize = side*labHeight;
            
            offsetX = (w - hSize)/2;
            offsetY = (h - vSize)/2;
        }
        Graphics2D g2d = (Graphics2D) g;
        
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        rh.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setRenderingHints(rh);
        
        g2d.setColor(Color.black);

        Dimension size = getSize();
        Insets insets = getInsets();

        int w = size.width - insets.left - insets.right;
        int h = size.height - insets.top - insets.bottom;
        
        g2d.fillRect(0, 0, w, h);
        
        doDrawing(g2d);
    }

    @Override
    public void onLabyrinthUpdated(Labyrinth data) {
        this.labyrinth = data;
        this.repaint();
    }

    @Override
    public void onRobotLocationChanged(Map<String, RobotPosition> robotPositions) {
        // do nothing
    }
    

}
