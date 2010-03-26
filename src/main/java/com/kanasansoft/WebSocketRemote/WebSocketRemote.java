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
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebSocketRemote implements OnMessageObserver{

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
		ServletHandler wsServletHandler = new ServletHandler();
		wsServletHandler.addServlet(new ServletHolder(wsServlet));
		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] {resourceHandler, wsServletHandler});
		server.setHandler(handlerList);
		server.start();

	}

	@Override
	public void onMessage(byte frame, String data) {
	}

	@Override
	public void onMessage(byte frame, byte[] data, int offset, int length) {
	}

}
