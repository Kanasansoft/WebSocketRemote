package com.kanasansoft.WebSocketRemote;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;

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
		public void onMessageAll(byte frame, String data) {
			for(WebSocketDesktop client : clients){
				try {
					client.outbound.sendMessage(frame, data);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		public void onMessageAll(byte frame, byte[] data, int offset, int length) {
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
