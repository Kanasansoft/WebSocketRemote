package com.kanasansoft.WebSocketRemote;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

class Capture extends Thread {
	private ScreenData screenData = new ScreenData();
	private OnCaptureObserver onCaptureObserver = null;
	@Override
	public void run() {
		int count=0;
		int[] size=new int[]{1,3,5};
		try{
			while(true){

				long startTime = new Date().getTime();

				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice[] gds = ge.getScreenDevices();
				Rectangle rect=new Rectangle(0,0,-1,-1);
				for(int i=0;i<gds.length;i++){
					Rectangle bounds = gds[i].getDefaultConfiguration().getBounds();
					rect.add(bounds);
				}

				BufferedImage bf = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_3BYTE_BGR);
				for(int i=0;i<gds.length;i++){
					Rectangle rectMonitor = gds[i].getDefaultConfiguration().getBounds();
					Rectangle captureArea = new Rectangle(rectMonitor.width, rectMonitor.height);
					BufferedImage bfMonitor = new Robot(gds[i]).createScreenCapture(captureArea);
					bf.getSubimage(rectMonitor.x-rect.x, rectMonitor.y-rect.y, rectMonitor.width, rectMonitor.height).setData(bfMonitor.getData());
				}

				Graphics graphics = bf.createGraphics();
				PointerInfo pointerInfo = MouseInfo.getPointerInfo();
				if(pointerInfo!=null){
					Point mousePoint = pointerInfo.getLocation();
					graphics.setColor(new Color(0,0,0));
					graphics.drawOval(mousePoint.x-rect.x-size[count]+0, mousePoint.y-rect.y-size[count]+0, size[count]*2+0, size[count]*2+0);
					graphics.setColor(new Color(255,255,255));
					graphics.drawOval(mousePoint.x-rect.x-size[count]+1, mousePoint.y-rect.y-size[count]+1, size[count]*2-2, size[count]*2-2);
					graphics.drawOval(mousePoint.x-rect.x-size[count]-1, mousePoint.y-rect.y-size[count]-1, size[count]*2+2, size[count]*2+2);
				}

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bf,"jpeg",baos);
				byte[] bytes = baos.toByteArray();
				byte[] base64 = Base64.encodeBase64(bytes);

				setScreenData(base64, rect);

				if(onCaptureObserver!=null){
					onCaptureObserver.onCapture(screenData);
				}

				count--;
				if(count<0){
					count=size.length-1;
				}

				long endTime = new Date().getTime();

				sleep(Math.max(Math.min((endTime-startTime)*2,1000),100));

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
