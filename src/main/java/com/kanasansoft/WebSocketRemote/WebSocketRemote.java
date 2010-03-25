package com.kanasansoft.WebSocketRemote;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
public class WebSocketRemote {

	public static void main(String[] args) throws Exception {
		new WebSocketRemote();
	}

	public WebSocketRemote() throws Exception {
		Server server = new Server(8088);
		ResourceHandler resourceHandler = new ResourceHandler();
		String htmlPath = this.getClass().getClassLoader().getResource("html").toExternalForm();
		resourceHandler.setResourceBase(htmlPath);
		WSServlet wsServlet = new WSServlet();
		ServletHandler wsServletHandler = new ServletHandler();
		wsServletHandler.addServlet(new ServletHolder(wsServlet));
		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] {resourceHandler, wsServletHandler});
		server.setHandler(handlerList);
		server.start();
//		server.join();

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

	}

	class Capture extends Thread {
		private ScreenData screenData = new ScreenData();
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
					String encoded = new String(base64);
					screenData.set(encoded, rect);
					sleep(500);
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
		ScreenData getCaptureString(){
			return screenData.get();
		}
		class ScreenData {
			String encoded = null;
			Rectangle rect = null;
			public ScreenData() {
				this.encoded = null;
				this.rect = null;
			}
			public ScreenData(String encoded, Rectangle rect) {
				this.encoded = new String(encoded);
				this.rect = new Rectangle(rect.x, rect.y, rect.width, rect.height);
			}
			synchronized void set(String base64, Rectangle rect) {
				this.encoded = base64;
				this.rect = rect;
			}
			synchronized ScreenData get() {
				if(this.encoded==null||this.rect==null){
					return null;
				}
				return new ScreenData(this.encoded, this.rect);
			}
		}
	}

	class WSServlet extends WebSocketServlet {
		@Override
		protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
			return new WebSocketDesktop();
		}
	}

	static class WebSocketDesktop implements WebSocket {
		static Set<WebSocketDesktop> clients = new CopyOnWriteArraySet<WebSocketDesktop>();
		Outbound outbound;
		@Override
		public void onConnect(Outbound outbound) {
			System.out.println("connect : "+this);
			this.outbound = outbound;
			clients.add(this);
		}
		@Override
		public void onDisconnect() {
			System.out.println("disconnect : "+this);
			clients.remove(this);
		}
		@Override
		public void onMessage(byte frame, String data) {
		}
		@Override
		public void onMessage(byte frame, byte[] data, int offset, int length) {
		}
		static public void sendMessageAll(byte frame, String data) {
			for(WebSocketDesktop client : clients){
				try {
					client.outbound.sendMessage(frame, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		static public void sendMessageAll(byte frame, byte[] data, int offset, int length) {
			for(WebSocketDesktop client : clients){
				try {
					client.outbound.sendMessage(frame, data, offset, length);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
