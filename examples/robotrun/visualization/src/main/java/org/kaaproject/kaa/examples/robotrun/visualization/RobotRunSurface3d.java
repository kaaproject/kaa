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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;

import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderType;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Cell;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Direction;
import org.kaaproject.kaa.examples.robotrun.labyrinth.Labyrinth;
import org.kaaproject.kaa.examples.robotrun.labyrinth.RobotPosition;
import org.kaaproject.kaa.examples.robotrun.visualization.Context.RobotRunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;

public class RobotRunSurface3d extends GLJPanel implements RobotRunListener {
    
    private static final long serialVersionUID = 1L;
    
    private org.kaaproject.kaa.examples.robotrun.visualization.Context context;
    private Labyrinth labyrinth;
    private Map<String, RobotPosition> robotPositions;
    private GLMaze glMaze;
    
    public RobotRunSurface3d(org.kaaproject.kaa.examples.robotrun.visualization.Context context, 
            Labyrinth labyrinth, 
            Map<String, RobotPosition> robotPositions, 
            boolean fullScreen) {
        
        this.context = context;
        this.labyrinth = labyrinth;
        this.robotPositions = robotPositions;
        
        glMaze = new GLMaze(labyrinth, robotPositions);
        
        MouseListener mazeMouse = glMaze.new MazeMouseAdapter();
        new AWTMouseAdapter(mazeMouse, this).addTo(this);
        
        addGLEventListener( new GLEventListener() {
            
            @Override
            public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
                glMaze.setup( glautodrawable.getGL().getGL2(), width, height );
            }
            
            @Override
            public void init( GLAutoDrawable glautodrawable ) {
                glMaze.init(glautodrawable);
            }
            
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
            }
            
            @Override
            public void display( GLAutoDrawable glautodrawable ) {
                glMaze.render( glautodrawable.getGL().getGL2(), glautodrawable.getSurfaceWidth(), glautodrawable.getSurfaceHeight() );
            }
        });
        
        if (!fullScreen) {
            this.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2){
                        RobotRunSurface3d surface3d = 
                                new RobotRunSurface3d(RobotRunSurface3d.this.context,
                                        RobotRunSurface3d.this.labyrinth,
                                        RobotRunSurface3d.this.robotPositions,
                                        true);
                        RobotRunSurface.showFullScreen(RobotRunSurface3d.this.context,
                                surface3d, surface3d, e);
                    }
                }
            });
        }
        
        final Animator animator = new Animator();
        animator.add(this);
        
        this.addHierarchyListener(new HierarchyListener() {
            
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) > 0) {
                    if (RobotRunSurface3d.this.isShowing()) {
                        animator.start();
                    }
                    else {
                        animator.stop();
                    }
                }
                
            }
        });
    }

    @Override
    public void onLabyrinthUpdated(Labyrinth data) {
        glMaze.setLabyrinth(data);
        repaint();
        
    }

    @Override
    public void onRobotLocationChanged(Map<String, RobotPosition> robotPositions) {
        glMaze.setRobotPositions(robotPositions);
        repaint();
    }

}

class GLMaze {
    

    private static final Logger LOG = LoggerFactory.getLogger(GLMaze.class);

    
    private static final float[] TRANSPARENT_DIFFUSE = new float[]{0f, 0f, 0f, 0.0f};
    private static final float[] UNKNOWN_BORDER_DIFFUSE = new float[]{1f, 1f, 1f, 1.0f};//Color.white;
    private static final float[] SOLID_BORDER_DIFFUSE = new float[]{1f, 1f, 0f, 1.0f};
    
    private static final float[] PLATFORM_DIFFUSE = new float[]{0.2f, 0.2f, 0.2f, 1.0f};
    
    private static final float[] DEAD_END_DIFFUSE = new float[]{0.7f, 0.7f, 0.7f, 1.0f};
    
    private Labyrinth labyrinth;
    private Map<String, RobotPosition> robotPositions;
    
    private float offsetX, offsetY, side;
    private int width, height;//, size;
    private float left = -1f;
    private float top = 1f;
    private float size = 2f;
    
    private int prevMouseX, prevMouseY;
    private float view_rotx = 0.0f, view_roty = 0.0f;
    private float view_rotz = 0.0f;
    private float view_zoom = 1f;
    
    private GLU glu;
    private GLUT glut;
    private TextRenderer renderer;    
    
    GLMaze(Labyrinth labyrinth, Map<String, RobotPosition> robotPositions) {
        this.labyrinth = labyrinth;
        this.robotPositions = robotPositions;
    }
    
    protected void setLabyrinth(Labyrinth labyrinth) {
        this.labyrinth = labyrinth;
    }
    
    protected void setRobotPositions(Map<String, RobotPosition> robotPositions) {
        this.robotPositions = robotPositions;
    }
    
    protected void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        glut = new GLUT();
        renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 72));
        //
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glDepthFunc(GL.GL_LESS);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
        gl.glEnable (GL2.GL_POLYGON_SMOOTH);
        gl.glEnable (GL2.GL_LINE_SMOOTH);
        gl.glHint(GL2.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST); 
        gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL.GL_NICEST); 
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); 
        gl.glEnable(GL.GL_BLEND);                         // Enable Blending
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);          // Type Of Blending To Use        
        gl.glEnable(GLLightingFunc.GL_NORMALIZE);

    }
    
    protected void setup( GL2 gl2, int w, int h ) {
        this.width = w;
        this.height = h;
        
        int startX = 0;
        int startY = 0;
        
        if (this.height > this.width) {
            startY = (this.height - this.width)/2;
            this.height = this.width;
            
        }
        
        gl2.setSwapInterval(1);

        doLook(gl2);
        gl2.glViewport( startX, startY, width, height );
    }
    
    private float transX(float x) {
        return left + x;
    }
    
    private float transY(float y) {
        return top - y;
    }
    
    float[] lightPos = { 0f,0f,0f,1f };        // light position
    float[] noAmbient = { 0.f, 0.f, 0.f, 1f };     // low ambient light
    float[] spec = { 1f, 1f, 1f, 1f }; // low ambient light
    float[] diffuse = { 1f, 1f, 1f, 1f };        // full diffuse colour
    
    float[] matSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };
    
    private void doLook(GL2 gl2) {
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();
        
        glu.gluPerspective(70f, (float)width / (float)height, 0.1f, 100f);
        
        float eyeX = 0;
        float eyeY = -1.5f;
        float eyeZ = 2f;
        
        glu.gluLookAt (eyeX, eyeY, eyeZ, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
        
        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

    }
    
    private void doLighting(GL2 gl) {
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);    
        
        gl.glPushMatrix();
        gl.glTranslatef(0.0f, -2f, 3.0f);
        
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, noAmbient, 0);
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPECULAR, spec, 0);
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, lightPos, 0);
        
        gl.glPopMatrix();
        
        gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, matSpecular, 0);
        gl.glMaterialf(GL.GL_FRONT, GLLightingFunc.GL_SHININESS, 25.0f);
    }

    protected void render( GL2 gl2, int width, int height ) {
        
        gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        gl2.glPushMatrix();
        gl2.glRotatef(view_rotx, 1.0f, 0.0f, 0.0f);
        gl2.glRotatef(view_roty, 0.0f, 1.0f, 0.0f);
        gl2.glRotatef(view_rotz, 0.0f, 0.0f, 1.0f);
        gl2.glScalef(view_zoom, view_zoom, view_zoom);
        
        doLighting(gl2);

        gl2.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, PLATFORM_DIFFUSE, 0);
        
        gl2.glPushMatrix();
        gl2.glTranslatef(0, 0, -0.05f);
        gl2.glScalef(4f, 4f, 0.1f);
        glut.glutSolidCube(1f);
        gl2.glPopMatrix();

        
        if (!labyrinth.isEmpty()) {
            
            int labWidth = labyrinth.getWidth();
            int labHeight = labyrinth.getHeight();

            side = Math.min(size/(float)labWidth, size/(float)labHeight);
                    
            float hSize = side*labWidth;
            float vSize = side*labHeight;

            offsetX = (size - hSize)/2;
            offsetY = (size - vSize)/2;
            
            for (int y=0;y<labHeight;y++) {
                for (int x=0;x<labWidth;x++) {
                    Cell cell = labyrinth.getCell(x, y);
                    if(cell.isDeadEnd()){
                        gl2.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, DEAD_END_DIFFUSE, 0);
                        gl2.glPushMatrix();
                        gl2.glTranslatef(transX(offsetX + x * side + side/2), transY(offsetY + y * side + side/2), side*0.025f);
                        gl2.glScalef(side, side, side*0.05f);
                        glut.glutSolidCube(1f);
                        gl2.glPopMatrix();
                    }
                    
                    drawBorder(gl2, offsetX, offsetY, side, x, y, cell, Direction.NORTH);
                    drawBorder(gl2, offsetX, offsetY, side, x, y, cell, Direction.WEST);
                    if (y==labHeight-1) {
                        drawBorder(gl2, offsetX, offsetY, side, x, y, cell, Direction.SOUTH);
                    }
                    if (x==labWidth-1) {
                        drawBorder(gl2, offsetX, offsetY, side, x, y, cell, Direction.EAST);
                    }
                }
            }
            
            for (String key : robotPositions.keySet()) {
                RobotPosition position = robotPositions.get(key);
                Cell cell = position.getCell();
                float[] diffuse = new Color(key.hashCode()).getRGBComponents(null);
//                gl2.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
//                gl2.glPushMatrix();
//                
//                gl2.glTranslatef(transX(offsetX + cell.getX() * side + side/2), transY(offsetY + cell.getY() * side + side/2), side/4);
//                gl2.glScalef(side/2, side/2, side/2);
//                glut.glutSolidSphere(0.5f, 20, 16);
//                gl2.glPopMatrix();
                
                drawRobot(gl2, offsetX, offsetY, side, cell.getX(), cell.getY(), diffuse);
                
                gl2.glPushMatrix();
                String text = position.getName();
                
                renderer.begin3DRendering();
                gl2.glDisable(GL2.GL_DEPTH_TEST);
                gl2.glEnable(GL2.GL_CULL_FACE);
                
                Rectangle2D bounds = renderer.getBounds(text);
                float textWidth = (float) bounds.getWidth();
                float textHeight = (float) bounds.getHeight();
                
                float textScaleFactor = side / (textWidth);

                gl2.glTranslatef(transX(offsetX + cell.getX() * side + side/2), 
                        transY(offsetY + cell.getY() * side + side/2), 
                        side/2);
                //when eye, then rotate z
                gl2.glRotatef(-view_rotz, 0.0f, 0.0f, 1.0f);

                
                renderer.draw3D(text,
                        - textWidth*textScaleFactor/2f, 
                        + textHeight*textScaleFactor/2f,
                        0,                        
                textScaleFactor);

                renderer.end3DRendering();
                
                gl2.glPopMatrix();
                
                gl2.glEnable(GL2.GL_DEPTH_TEST);
            }
        
        }
        gl2.glPopMatrix();
    }
    
    private void drawRobot(GL2 gl2,
            float offsetX, 
            float offsetY, 
            float side, 
            int x, 
            int y,
            float[] diffuse) {
        gl2.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
        
        gl2.glPushMatrix();
        
        gl2.glTranslatef(transX(offsetX + x * side + side/2), transY(offsetY + y * side + side/2), side/4);
        gl2.glScalef(side/2, side/2, side/2);

        gl2.glPushMatrix();
        gl2.glTranslatef(0.5f, 0.6f, -0.05f);
        gl2.glRotatef(90, 0f, 1f, 0f);
        glut.glutSolidCylinder(0.4f, 0.2f, 10, 20);
        gl2.glPopMatrix();

        gl2.glPushMatrix();
        gl2.glTranslatef(-0.7f, 0.6f, -0.05f);
        gl2.glRotatef(90, 0f, 1f, 0f);
        glut.glutSolidCylinder(0.4f, 0.2f, 10, 20);
        gl2.glPopMatrix();

        gl2.glPushMatrix();
        gl2.glTranslatef(0.5f, -0.6f, -0.05f);
        gl2.glRotatef(90, 0f, 1f, 0f);
        glut.glutSolidCylinder(0.4f, 0.2f, 10, 20);
        gl2.glPopMatrix();
        
        gl2.glPushMatrix();
        gl2.glTranslatef(-0.7f, -0.6f, -0.05f);
        gl2.glRotatef(90, 0f, 1f, 0f);
        glut.glutSolidCylinder(0.4f, 0.2f, 10, 20);
        gl2.glPopMatrix();

        gl2.glPushMatrix();
        gl2.glTranslatef(0f, 0f, -0.05f);
        gl2.glScalef(1f, 1.2f, 0.25f);
        glut.glutSolidCube(1f);
        gl2.glPopMatrix();

        gl2.glPushMatrix();
        gl2.glTranslatef(0f, 0f, -0.05f + 0.25f/2f + 0.4f/2f);
        gl2.glScalef(0.9f, 1.0f, 0.4f);
        glut.glutSolidCube(1f);
        gl2.glPopMatrix();
        
        gl2.glPopMatrix();
        
    }
    
    private void drawBorder(GL2 gl2,
            float offsetX, 
            float offsetY, 
            float side, 
            int x, 
            int y, 
            Cell cell, 
            Direction direction) {
        
        BorderType type = cell.getBorder(direction);
        if (type != BorderType.FREE) {
            float[] diffuse = getDiffuse(type);
            if (type == BorderType.UNKNOWN) {
                gl2.glDisable(GL2.GL_LIGHTING);
                gl2.glColor4fv(diffuse, 0);
            }
            else {
                gl2.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_DIFFUSE, diffuse, 0);
            }
            
            gl2.glPushMatrix();
            
            float borderHeight = type == BorderType.UNKNOWN ? side*0.05f : side*0.65f;
            float borderThickness = side*0.05f;
            
            switch (direction) {
            case NORTH:
                gl2.glTranslatef(transX(offsetX + x * side + side/2), transY(offsetY + y * side), borderHeight/2);
                gl2.glScalef(side, borderThickness, borderHeight);
                break;
            case WEST:
                gl2.glTranslatef(transX(offsetX + x * side), transY(offsetY + y * side + side/2), borderHeight/2);
                gl2.glScalef(borderThickness, side, borderHeight);
                break;
            case SOUTH:
                gl2.glTranslatef(transX(offsetX + x * side + side/2), transY(offsetY + (y+1) * side), borderHeight/2);
                gl2.glScalef(side, borderThickness, borderHeight);
                break;
            case EAST:
                gl2.glTranslatef(transX(offsetX + (x+1) * side), transY(offsetY + y * side + side/2), borderHeight/2);
                gl2.glScalef(borderThickness, side, borderHeight);
                break;
            }
            if (type == BorderType.UNKNOWN) {
                glut.glutWireCube(1f);
            }
            else {
                glut.glutSolidCube(1f);
            }
            gl2.glPopMatrix();
            if (type == BorderType.UNKNOWN) {
                gl2.glEnable(GL2.GL_LIGHTING);
            }
            
        }
    }
    
    private float[] getDiffuse(BorderType type) {
        switch (type) {
        case UNKNOWN:
            return UNKNOWN_BORDER_DIFFUSE;
        case SOLID:
            return SOLID_BORDER_DIFFUSE;
            default:
                return TRANSPARENT_DIFFUSE;
        }
    }
    
    class MazeMouseAdapter extends MouseAdapter {
        @Override
      public void mousePressed(MouseEvent e) {
          prevMouseX = e.getX();
          prevMouseY = e.getY();
        }

        @Override
      public void mouseReleased(MouseEvent e) {
        }

        @Override
      public void mouseDragged(MouseEvent e) {
          final int x = e.getX();
          final int y = e.getY();
          int width=0, height=0;
          Object source = e.getSource();
          if(source instanceof GLAutoDrawable) {
              GLAutoDrawable glad = (GLAutoDrawable) source;
              width=glad.getSurfaceWidth();
              height=glad.getSurfaceHeight();
          } else if (GLProfile.isAWTAvailable() && source instanceof java.awt.Component) {
              java.awt.Component comp = (java.awt.Component) source;
              width=comp.getWidth();
              height=comp.getHeight();
          } else {
              throw new RuntimeException("Event source neither Window nor Component: "+source);
          }
          float thetaZ = 360.0f * ( (float)(x-prevMouseX)/(float)width);
          float thetaX = 360.0f * ( (float)(prevMouseY-y)/(float)height);

          prevMouseX = x;
          prevMouseY = y;

          view_rotx -= thetaX;
          view_rotz += thetaZ;
        }
        
        @Override
        public void mouseWheelMoved(final MouseEvent e) {
            view_zoom += e.getRotation()[1]*0.1f;
        }
    }
    
}