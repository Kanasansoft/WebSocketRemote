package com.kanasansoft.WebSocketRemote;

import java.io.IOException;
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
		server.join();
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
