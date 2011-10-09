package com.kanasansoft.WebSocketRemote;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.websocket.WebSocket;

class WebSocketDesktop implements WebSocket {
	OnMessageObserver onMessageObserver = null;
	static Set<WebSocketDesktop> clients = new CopyOnWriteArraySet<WebSocketDesktop>();
	Outbound outbound;
	public WebSocketDesktop(OnMessageObserver onMessageObserver) {
		super();
		this.onMessageObserver = onMessageObserver;
	}
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
	synchronized public void onMessage(byte frame, String data) {
		onMessageObserver.onMessage(this.outbound, frame, data);
	}
	@Override
	synchronized public void onMessage(byte frame, byte[] data, int offset, int length) {
		onMessageObserver.onMessage(this.outbound, frame, data, offset, length);
	}
}
