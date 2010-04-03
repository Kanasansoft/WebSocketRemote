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
	public void onMessage(byte frame, String data) {
		onMessageObserver.onMessage(this.outbound, frame, data);
	}
	@Override
	public void onMessage(byte frame, byte[] data, int offset, int length) {
		onMessageObserver.onMessage(this.outbound, frame, data, offset, length);
	}
	@Deprecated
	static public void sendMessageAll(byte frame, String data) {
		for(WebSocketDesktop client : clients){
			try {
				client.outbound.sendMessage(frame, data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Deprecated
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
