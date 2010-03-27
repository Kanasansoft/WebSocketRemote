package com.kanasansoft.WebSocketRemote;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebSocketRemote implements OnMessageObserver{

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

		Server server = new Server(8088);

		ResourceHandler resourceHandler = new ResourceHandler();
		String htmlPath = this.getClass().getClassLoader().getResource("html").toExternalForm();
		resourceHandler.setResourceBase(htmlPath);

		WSServlet wsServlet = new WSServlet(this);
		ServletContextHandler wsServletContextHandler = new ServletContextHandler();
		wsServletContextHandler.setContextPath("/");
		server.setHandler(wsServletContextHandler);
		ServletHolder wsServletHolder = new ServletHolder(wsServlet);
		wsServletHolder.setInitParameter("bufferSize", Integer.toString(8192*128,10));
		wsServletContextHandler.addServlet(wsServletHolder, "/ws/*");
		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] {resourceHandler, wsServletContextHandler});
		server.setHandler(handlerList);
		server.start();

		Capture capture = new Capture();
		capture.start();

		while(true){
			screenData = capture.getScreenData();
			if(screenData!=null){
				String encoded = screenData.encoded;
				if(encoded!=null){
					WebSocketDesktop.sendMessageAll((byte)0, encoded);
				}
			}
			Thread.sleep(1000);
		}

	}

	@Override
	public void onMessage(byte frame, String data) {
	}

	@Override
	public void onMessage(byte frame, byte[] data, int offset, int length) {
	}

}
