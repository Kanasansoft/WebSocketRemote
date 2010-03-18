package com.kanasansoft.WebSocketRemote;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import org.apache.commons.codec.binary.Base64;

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
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		ImageIO.write(bf,"png",byteArrayOS);
		byte[] bytes = byteArrayOS.toByteArray();
		byte[] encoded = Base64.encodeBase64(bytes);
/*
		StringBuffer buffer=new StringBuffer();
		for(int i=0;i<encoded.length;i++){
			buffer.append(Integer.toString(encoded[i],16));
		}
		System.out.println(buffer.toString());
*/
		File file = new File("base64.txt");
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(encoded);
		fos.close();

	}
}
