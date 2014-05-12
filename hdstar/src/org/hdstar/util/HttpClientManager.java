package org.hdstar.util;

import java.util.concurrent.TimeUnit;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.params.ClientPNames;
import ch.boye.httpclientandroidlib.conn.ClientConnectionManager;
import ch.boye.httpclientandroidlib.conn.routing.HttpRoute;
import ch.boye.httpclientandroidlib.conn.scheme.PlainSocketFactory;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.scheme.SchemeRegistry;
import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.conn.PoolingClientConnectionManager;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.CoreConnectionPNames;
import ch.boye.httpclientandroidlib.params.HttpParams;
import ch.boye.httpclientandroidlib.params.HttpProtocolParams;

public class HttpClientManager {
	private static volatile HttpClient customHttpClient;

//	public static final String USER_AGENT = "Platinum Crawler";
    //部分站点会拒绝未知客户端（如hdw），直接用firefox
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; rv:29.0) Gecko/20100101 Firefox/29.0";

//	/**
//	 * 最大连接数
//	 */
//	public final static int MAX_TOTAL_CONNECTIONS = 800;
	/**
	 * 获取连接的最大等待时间
	 */
	public final static int WAIT_TIMEOUT = 60 * 1000;
//	/**
//	 * 每个路由最大连接数
//	 */
//	public final static int MAX_ROUTE_CONNECTIONS = 400;
	/**
	 * 连接超时时间
	 */
	public final static int CONNECT_TIMEOUT = 90 * 1000;
//	/**
//	 * 读取超时时间
//	 */
//	public final static int READ_TIMEOUT = 30 * 1000;

	// client更新周期
	private static final int EXPIRED_PERIOD = 5 * 60 * 1000;

	private static IdleConnectionMonitorThread idleThread;

	private HttpClientManager() {
	}

	public static HttpClient getHttpClient() {
		// long cur = System.currentTimeMillis();
		if (customHttpClient == null) {
			synchronized (HttpClientManager.class) {
				if (customHttpClient == null) {
					// HttpParams params = new BasicHttpParams();
					// params.setParameter(
					// CoreConnectionPNames.CONNECTION_TIMEOUT,
					// CONNECT_TIMEOUT);
					// params.setParameter(CoreConnectionPNames.SO_TIMEOUT,
					// WAIT_TIMEOUT);
					// params.setParameter(ClientPNames.HANDLE_REDIRECTS,
					// false);
					// HttpProtocolParams.setUserAgent(params, USER_AGENT);
					// SchemeRegistry schemeRegistry = new SchemeRegistry();
					// schemeRegistry.register(new Scheme("http", 80,
					// PlainSocketFactory.getSocketFactory()));
					// schemeRegistry.register(new Scheme("https", 443,
					// SSLSocketFactory.getSocketFactory()));
					// PoolingClientConnectionManager cm = new
					// PoolingClientConnectionManager(
					// schemeRegistry);
					// // Increase max total connection to 200
					// cm.setMaxTotal(200);
					// // Increase default max connection per route to 20
					// cm.setDefaultMaxPerRoute(20);
					// // Increase max connections for localhost:80 to 50
					// HttpHost localhost = new HttpHost("locahost", 80);
					// cm.setMaxPerRoute(new HttpRoute(localhost), 50);
					customHttpClient = newHttpClient();
					idleThread = new IdleConnectionMonitorThread(
							customHttpClient.getConnectionManager());
					idleThread.start();
				}
			}
		}
		return customHttpClient;
	}

	/**
	 * 
	 * 非单例，用完要shutdown. <br/>
	 * 
	 * @return
	 */
	public static HttpClient newHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
				CONNECT_TIMEOUT);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, WAIT_TIMEOUT);
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpProtocolParams.setUserAgent(params, USER_AGENT);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
				.getSocketFactory()));
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager(
				schemeRegistry);
		// Increase max total connection to 200
		cm.setMaxTotal(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(20);
		// Increase max connections for localhost:80 to 50
		HttpHost localhost = new HttpHost("locahost", 80);
		cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		return new DefaultHttpClient(cm, params);
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * 慎用！<br/>
	 * 重置HttpClient实例. <br/>
	 */
	public static void restClient() {
		if (customHttpClient != null) {
			customHttpClient.getConnectionManager().shutdown();
			customHttpClient = null;
			idleThread.shutdown();
		}
	}

	public static class IdleConnectionMonitorThread extends Thread {

		private final ClientConnectionManager connMgr;
		private volatile boolean shutdown;

		public IdleConnectionMonitorThread(ClientConnectionManager connMgr) {
			super();
			this.connMgr = connMgr;
		}

		@Override
		public void run() {
			try {
				while (!shutdown) {
					synchronized (this) {
						wait(EXPIRED_PERIOD);
						// Close expired connections
						connMgr.closeExpiredConnections();
						// Optionally, close connections
						// that have been idle longer than 30 sec
						connMgr.closeIdleConnections(60, TimeUnit.SECONDS);
					}
				}
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				// terminate
			}
		}

		public void shutdown() {
			shutdown = true;
			synchronized (this) {
				notifyAll();
			}
		}

	}

	// private static void
	// workAroundReverseDnsBugInHoneycombAndEarlier(HttpClient client) {
	// // Android had a bug where HTTPS made reverse DNS lookups (fixed in Ice
	// // Cream Sandwich)
	// // http://code.google.com/p/android/issues/detail?id=13117
	// SocketFactory socketFactory = new LayeredSocketFactory() {
	// SSLSocketFactory delegate = SSLSocketFactory.getSocketFactory();
	//
	// @Override
	// public Socket createSocket() throws IOException {
	// return delegate.createSocket();
	// }
	//
	// @Override
	// public Socket connectSocket(Socket sock, String host, int port,
	// InetAddress localAddress, int localPort, HttpParams params)
	// throws IOException {
	// return delegate.connectSocket(sock, host, port, localAddress,
	// localPort, params);
	// }
	//
	// @Override
	// public boolean isSecure(Socket sock)
	// throws IllegalArgumentException {
	// return delegate.isSecure(sock);
	// }
	//
	// @Override
	// public Socket createSocket(Socket socket, String host, int port,
	// boolean autoClose) throws IOException {
	// injectHostname(socket, host);
	// return delegate.createSocket(socket, host, port, autoClose);
	// }
	//
	// private void injectHostname(Socket socket, String host) {
	// try {
	// Field field = InetAddress.class
	// .getDeclaredField("hostName");
	// field.setAccessible(true);
	// field.set(socket.getInetAddress(), host);
	// } catch (Exception ignored) {
	// }
	// }
	// };
	// client.getConnectionManager().getSchemeRegistry()
	// .register(new Scheme("https", socketFactory, 443));
	// }
}
