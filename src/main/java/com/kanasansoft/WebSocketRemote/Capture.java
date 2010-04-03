package com.kanasansoft.WebSocketRemote;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

class Capture extends Thread {
	private ScreenData screenData = new ScreenData();
	private OnCaptureObserver onCaptureObserver = null;
	@Override
	public void run() {
		try{
			while(true){
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gds = ge.getScreenDevices();
				Rectangle rect=new Rectangle(0,0,-1,-1);
				for(int i=0;i<gds.length;i++){
					rect.add(gds[i].getDefaultConfiguration().getBounds());
				}
				Robot robot = new Robot();
				BufferedImage bf = robot.createScreenCapture(rect);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bf,"png",baos);
				byte[] bytes = baos.toByteArray();
				byte[] base64 = Base64.encodeBase64(bytes);
				setScreenData(base64, rect);
				if(onCaptureObserver!=null){
					onCaptureObserver.onCapture(screenData);
				}
				sleep(0);
			}
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(1);
	}
	synchronized private void setScreenData(byte[] base64, Rectangle rect){
		screenData.set(base64, rect);
	}
	synchronized ScreenData getScreenData(){
		return screenData.get();
	}
	void setOnCaptureObserver(OnCaptureObserver onCaptureObserver){
		this.onCaptureObserver = onCaptureObserver;
	}
}
