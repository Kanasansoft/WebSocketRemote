package com.kanasansoft.WebSocketRemote;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

class WSServlet extends WebSocketServlet {
	private static final long serialVersionUID = 1L;
	OnMessageObserver onMessageObserver = null;

	public WSServlet(OnMessageObserver onMessageObserver) {
		super();
		this.onMessageObserver = onMessageObserver;
	}
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		getServletContext().getNamedDispatcher("default").forward(request, response);
	}
	@Override
	protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new WebSocketDesktop(onMessageObserver);
	}
}
