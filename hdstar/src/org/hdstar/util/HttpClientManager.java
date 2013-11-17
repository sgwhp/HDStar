package org.hdstar.util;

import java.io.IOException;

import org.hdstar.common.Const;

import ch.boye.httpclientandroidlib.HttpHost;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.params.ClientPNames;
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

public class HttpClientManager {
	private static volatile HttpClient customHttpClient;

	/**
	 * 最大连接数
	 */
	public final static int MAX_TOTAL_CONNECTIONS = 800;
	/**
	 * 获取连接的最大等待时间
	 */
	public final static int WAIT_TIMEOUT = 60 * 1000;
	/**
	 * 每个路由最大连接数
	 */
	public final static int MAX_ROUTE_CONNECTIONS = 400;
	/**
	 * 连接超时时间
	 */
	public final static int CONNECT_TIMEOUT = 60 * 1000;
	/**
	 * 读取超时时间
	 */
	public final static int READ_TIMEOUT = 10000;
	
	//client更新周期
	private static final int VALIDATE_PERIOD = 10 * 60 * 1000;
	//client最后一次使用时间
	private static long latest;

	private HttpClientManager() {
	}

	public static HttpClient getHttpClient() {
		// if (customHttpClient == null) {
		// synchronized (CustomHttpClient.class){
		// if(customHttpClient == null){
		// HttpParams params = new BasicHttpParams();
		// HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		// HttpProtocolParams.setContentCharset(params,
		// HTTP.DEFAULT_CONTENT_CHARSET);
		// HttpProtocolParams.setUseExpectContinue(params, true);
		// HttpProtocolParams.setUserAgent(params,
		// "Mozilla/5.0(Linux; U; Android 2.2.1; en-us; Nexus One Build/FRG83)AppleWebkit/533.1(KHTML, like Gecko) Version/4.0 Moblie Safari/533.1");
		// ConnManagerParams.setTimeout(params, 1000);
		// HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
		// HttpConnectionParams.setSoTimeout(params, WAIT_TIMEOUT);
		// SchemeRegistry schReg = new SchemeRegistry();
		// schReg.register(new Scheme("https",
		// SSLSocketFactory.getSocketFactory(), 443));
		// schReg.register(new Scheme("http",
		// PlainSocketFactory.getSocketFactory(), 80));
		// ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
		// params, schReg);
		// customHttpClient = new DefaultHttpClient(conMgr, params);
		// workAroundReverseDnsBugInHoneycombAndEarlier(customHttpClient);
		// }
		// }
		//
		// HttpRequestRetryHandler myRetryHandler = new
		// HttpRequestRetryHandler() {
		// @Override
		// public boolean retryRequest(IOException exception,
		// int executionCount, HttpContext context) {
		// if (executionCount >= 5) {
		// // Do not retry if over max retry count
		// return false;
		// }
		// if (exception instanceof NoHttpResponseException) {
		// // Retry if the server dropped connection on us
		// return true;
		// }
		// if (exception instanceof SSLHandshakeException) {
		// // Do not retry on SSL handshake exception
		// return false;
		// }
		// HttpRequest request = (HttpRequest) context
		// .getAttribute(ExecutionContext.HTTP_REQUEST);
		// boolean idempotent = !(request instanceof
		// HttpEntityEnclosingRequest);
		// if (idempotent) {
		// // Retry if the request is considered idempotent
		// return true;
		// }
		// return false;
		// }
		// };
		// ((AbstractHttpClient) customHttpClient)
		// .setHttpRequestRetryHandler(myRetryHandler);
		// }
		long cur = System.currentTimeMillis();
		if (customHttpClient == null) {
			synchronized (HttpClientManager.class) {
				if (customHttpClient == null) {
					HttpParams params = new BasicHttpParams();
					params.setParameter(
							CoreConnectionPNames.CONNECTION_TIMEOUT,
							CONNECT_TIMEOUT);
					params.setParameter(CoreConnectionPNames.SO_TIMEOUT,
							WAIT_TIMEOUT);
					params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
					SchemeRegistry schemeRegistry = new SchemeRegistry();
					schemeRegistry.register(new Scheme("http", 80,
							PlainSocketFactory.getSocketFactory()));
					schemeRegistry.register(new Scheme("https", 443,
							SSLSocketFactory.getSocketFactory()));
					PoolingClientConnectionManager cm = new PoolingClientConnectionManager(
							schemeRegistry);
					// Increase max total connection to 200
					cm.setMaxTotal(200);
					// Increase default max connection per route to 20
					cm.setDefaultMaxPerRoute(20);
					// Increase max connections for localhost:80 to 50
					HttpHost localhost = new HttpHost("locahost", 80);
					cm.setMaxPerRoute(new HttpRoute(localhost), 50);
					customHttpClient = new DefaultHttpClient(cm, params);
				}
			}
		} else if(cur - latest > VALIDATE_PERIOD){
			synchronized(HttpClientManager.class){
				if(cur - latest > VALIDATE_PERIOD){
					final HttpGet get = new HttpGet(Const.Urls.SERVER_ADDRESS);
					activateClient(get);
					new Thread(){
						public void run(){
							try {
								//连接打开后关闭并返回
								Thread.sleep(200);
								get.abort();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}.start();
					latest = cur;
				}
			}
		}

		latest = cur;
		return customHttpClient;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * client一段时间不使用后，会出现长时间无法连接的情况，需要新建一个连接并关闭它。
	 */
	private static void activateClient(HttpGet get){
		try {
			customHttpClient.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void restClient() {
		if (customHttpClient != null) {
			customHttpClient.getConnectionManager().shutdown();
			customHttpClient = null;
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