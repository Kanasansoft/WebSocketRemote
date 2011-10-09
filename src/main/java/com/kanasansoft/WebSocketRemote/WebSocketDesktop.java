package com.kanasansoft.WebSocketRemote;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.websocket.WebSocket;

class WebSocketDesktop implements WebSocket.OnTextMessage {
	OnMessageObserver onMessageObserver = null;
	static Set<WebSocketDesktop> clients = new CopyOnWriteArraySet<WebSocketDesktop>();
	Connection connection;
	public WebSocketDesktop(OnMessageObserver onMessageObserver) {
		super();
		this.onMessageObserver = onMessageObserver;
	}
	@Override
	public void onOpen(Connection connection) {
		System.out.println("connect : "+this);
		this.connection = connection;
		clients.add(this);
	}
	@Override
	public void onClose(int closeCode, String message) {
		System.out.println("disconnect : "+this);
		clients.remove(this);
	}
	@Override
	synchronized public void onMessage(String data) {
		onMessageObserver.onMessage(this.connection, data);
	}
}
