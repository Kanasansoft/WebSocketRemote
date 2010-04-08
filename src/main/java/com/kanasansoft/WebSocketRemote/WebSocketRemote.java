package com.kanasansoft.WebSocketRemote;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.Outbound;

public class WebSocketRemote implements OnMessageObserver, OnCaptureObserver{

	Robot robot = new Robot();
	ScreenData screenData = null;

	public static void main(String[] args) throws Exception {
		new WebSocketRemote();
	}

	public WebSocketRemote() throws Exception {
		MenuItem quitMenuItem = new MenuItem("Quit");
		quitMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		PopupMenu popupMenu = new PopupMenu();
		popupMenu.add(quitMenuItem);

		URL imageUrl = this.getClass().getClassLoader().getResource("images/icon.png");
		TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(imageUrl));
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip("WebSocketRemote");
		trayIcon.setPopupMenu(popupMenu);

		SystemTray systemTray = java.awt.SystemTray.getSystemTray();
		systemTray.add(trayIcon);

		Server server = new Server(40320);

		ResourceHandler resourceHandler = new ResourceHandler();
		String htmlPath = this.getClass().getClassLoader().getResource("html").toExternalForm();
		resourceHandler.setResourceBase(htmlPath);

		WSServlet wsServlet = new WSServlet(this);
		ServletContextHandler wsServletContextHandler = new ServletContextHandler();
		ServletHolder wsServletHolder = new ServletHolder(wsServlet);
		wsServletHolder.setInitParameter("bufferSize", Integer.toString(8192*256,10));
		wsServletContextHandler.addServlet(wsServletHolder, "/ws/*");
		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] {resourceHandler, wsServletContextHandler});
		server.setHandler(handlerList);
		server.start();

		Capture capture = new Capture();
		capture.setOnCaptureObserver(this);
		capture.start();

	}

	void sendCaptureImage(Outbound outbound){
		byte separatorByte = "_".getBytes()[0];
		if(screenData==null){return;}
		byte[] base64 = screenData.base64;
		if(base64==null){return;}
		int sendSize = 8192;
		int sendCount = base64.length / sendSize;
		int remainder = base64.length % sendSize;
		if(remainder!=0){
			sendCount++;
		}
		byte[] imageId = Long.toString(new Date().getTime(),16).getBytes();
		byte[] sequenceCount = Integer.toString(sendCount, 16).getBytes();
		for(int i=0;i<sendCount;i++){
			byte[] sequenceNumber = Integer.toString(i + 1, 16).getBytes();
			int restLength = base64.length-i * sendSize;
			int sendLength = sendSize<restLength?sendSize:restLength;
			byte[] sendData = new byte[imageId.length + sequenceNumber.length + sequenceCount.length + sendLength + 3];
			System.arraycopy(imageId, 0, sendData, 0, imageId.length);
			System.arraycopy(sequenceNumber, 0, sendData, imageId.length + 1, sequenceNumber.length);
			System.arraycopy(sequenceCount, 0, sendData, imageId.length + sequenceNumber.length + 2, sequenceCount.length);
			System.arraycopy(base64, i * sendSize, sendData, imageId.length + sequenceNumber.length + sequenceCount.length + 3, sendLength);
			sendData[imageId.length + 0] = separatorByte;
			sendData[imageId.length + sequenceNumber.length + 1] = separatorByte;
			sendData[imageId.length + sequenceNumber.length + sequenceCount.length + 2] =separatorByte;
			try {
				outbound.sendMessage((byte)WebSocket.SENTINEL_FRAME, sendData,0,sendData.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void onMouseMove(String data){

		if(data==null){return;}
		if(data.equals("")){return;}
		String[] messages = data.split(",");
		if(messages.length!=2){return;}

		int x = Integer.parseInt(messages[0]);
		int y = Integer.parseInt(messages[1]);

		if(screenData==null){return;}
		x += screenData.rect.x;
		y += screenData.rect.y;

		robot.waitForIdle();
		robot.mouseMove(x, y);
		robot.waitForIdle();

	}

	void onMouseDown(String data){

		if(MouseInfo.getPointerInfo()==null){return;}

		if(data==null){return;}
		if(data.equals("")){return;}
		String[] messages = data.split(",");
		if(messages.length!=1){return;}

		String buttonString = messages[0];
		int button = 0;
		if(buttonString.equals("main")){
			button = InputEvent.BUTTON1_MASK;
		}else if(buttonString.equals("wheel")){
			button = InputEvent.BUTTON2_MASK;
		}else if(buttonString.equals("contextmenu")){
			button = InputEvent.BUTTON3_MASK;
		}
		if(button==0){return;}

		robot.waitForIdle();
		robot.mousePress(button);
		robot.waitForIdle();

	}

	void onMouseUp(String data){

		if(MouseInfo.getPointerInfo()==null){return;}

		if(data==null){return;}
		if(data.equals("")){return;}
		String[] messages = data.split(",");
		if(messages.length!=1){return;}

		String buttonString = messages[0];
		int button = 0;
		if(buttonString.equals("main")){
			button = InputEvent.BUTTON1_MASK;
		}else if(buttonString.equals("wheel")){
			button = InputEvent.BUTTON2_MASK;
		}else if(buttonString.equals("contextmenu")){
			button = InputEvent.BUTTON3_MASK;
		}
		if(button==0){return;}

		robot.waitForIdle();
		robot.mouseRelease(button);
		robot.waitForIdle();

	}

	void onMouseWheel(String data){

		if(MouseInfo.getPointerInfo()==null){return;}

		if(data==null){return;}
		if(data.equals("")){return;}
		String[] messages = data.split(",");
		if(messages.length!=1){return;}

		int wheel = Integer.parseInt(messages[0]);
		if(wheel>0){
			wheel=-1;
		}else if(wheel<0){
			wheel=1;
		}

		robot.waitForIdle();
		robot.mouseWheel(wheel);
		robot.waitForIdle();

	}

	@Override
	@Deprecated
	synchronized public void onMessage(byte frame, String data) {
	}

	@Override
	@Deprecated
	synchronized public void onMessage(byte frame, byte[] data, int offset, int length) {
	}

	@Override
	synchronized public void onMessage(Outbound outbound, byte frame, String data) {
		if(data==null){return;}
		if(data.equals("")){return;}
		String[] messages = data.split(",",2);
		if(messages.length==0){return;}
		String messageType = messages[0];
		String messageData = messages.length==1?"":messages[1];
		if(messageType.equals("image")){
			sendCaptureImage(outbound);
		}else if(messageType.equals("mousemove")){
			onMouseMove(messageData);
		}else if(messageType.equals("mousedown")){
			onMouseDown(messageData);
		}else if(messageType.equals("mouseup")){
			onMouseUp(messageData);
		}else if(messageType.equals("mousewheel")){
			onMouseWheel(messageData);
		}
	}

	@Override
	synchronized public void onMessage(Outbound outbound, byte frame, byte[] data, int offset, int length) {
	}

	@Override
	synchronized public void onCapture(ScreenData screenData) {
		this.screenData=screenData;
	}

}
