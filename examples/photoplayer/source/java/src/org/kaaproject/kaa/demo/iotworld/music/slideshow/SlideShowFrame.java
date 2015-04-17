package org.kaaproject.kaa.demo.iotworld.music.slideshow;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class SlideShowFrame extends JFrame {

    private static final long serialVersionUID = 4151179253925892583L;

    public SlideShowFrame() {
        getContentPane().setBackground(Color.BLACK);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
    }
}