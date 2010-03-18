package com.kanasansoft.WebSocketRemote;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

public class Capture {

	public static void main(String[] args) throws AWTException, IOException {
		System.out.println(new Date().getTime());
		new Capture().capture();
		System.out.println(new Date().getTime());
	}

	public void capture() throws AWTException, IOException{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gds = ge.getScreenDevices();
		Rectangle rect=new Rectangle(0,0,-1,-1);
		for(int i=0;i<gds.length;i++){
			rect.add(gds[i].getDefaultConfiguration().getBounds());
		}
		System.out.println(rect);
		Robot robot = new Robot();
		BufferedImage bf = robot.createScreenCapture(rect);
		ImageIO.write(bf,"png",new File("test.png"));
	}
}
