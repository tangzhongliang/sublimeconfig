package slack;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.internal.ws.RealWebSocket;
import okio.ByteString;

import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.json.JSONException;
import org.json.JSONObject;
import org.xlightweb.IWebSocketConnection;
import org.xlightweb.client.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TestSend {

	ExecutorService executorService = Executors.newScheduledThreadPool(3);

	// public String url =
	// "wss://mpmulti-me7x.advop.ricoh.co.jp.test.slack-msgs.com/websocket/9p8OkTT21Zd5b79lE1mRfiZlB6NW0HZkB7MC8NlrfjbfVwhagKvSeL_LYwZPnHl7UpXV1P6HEGZdsAPPXjVBubFMgDps-myryMLrWRXgQ8yOdwWc2bxc06mHxs1TDnHfn0J_xw-E1ZKwEhxZZklvxl2eld6C2aC53ErS9VZ9OTM=";
	public static void main(String[] args) {
		TestSend test = new TestSend();
//		 test.sendMessage();
		test.start();
//		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
//				"172.25.75.2", 8080));
//		SlackApi api = new SlackApi("https://hooks.slack.com/services/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",proxy);
//		api.call(new SlackMessage("from hook"));
		// test.send("D4TSNUH40", "asdfasdf");
		// test.test();

	}

	private void start() {
		// TODO Auto-generated method stub
		startBot();
		BufferedReader bReader = new BufferedReader(new InputStreamReader(
				System.in));
		while (true) {
			try {
				String input = bReader.readLine();
				if (input.equals("quit")) {
					mWebSocket.cancel();
				} else if (input.equals("start")) {
					startBot();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void send(String channel, String text) {
		HttpURLConnection connection = null;
		try {
			final URL url = new URL(
					"https://slack.com/api/chat.postMessage?token=xoxb-163906966384-EmuBPOcC8OplzMMdW2a50jtW&channel="
							+ channel
							+ "&text="
							+ URLEncoder.encode(text)
							+ "&pretty=1&username=cute");
			// final URL url = new URL("https://www.baidu.com");
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
					"172.25.75.2", 8080));
			connection = (HttpURLConnection) url.openConnection(proxy);
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(10000);
			connection.setUseCaches(false);
			connection.setDoInput(true);

			final InputStream is = connection.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(
					is));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			System.out.println(response.toString());
			// return response.toString();
		} catch (Exception e) {

		} finally {
			connection.disconnect();
		}
	}

	boolean flag = true;
	WebSocket mWebSocket = null;
	OkHttpClient mOkHttpClient = null;

	public void startBot() {
		final String url = getService();

		try {
			HttpURLConnection connection;
			// Proxy proxy = new Proxy(Proxy.Type.HTTP, new
			// InetSocketAddress("172.25.75.2", 8080));

			mOkHttpClient = new OkHttpClient.Builder()   
					.readTimeout(3000, TimeUnit.SECONDS)
					// 设置读取超时时间
					.proxy(new Proxy(Type.HTTP, new InetSocketAddress(
							"172.25.75.2", 8080)))
					.writeTimeout(3000, TimeUnit.SECONDS)// 设置写的超时时间
					.connectTimeout(3000, TimeUnit.SECONDS)// 设置连接超时时间
					.build();
			Request request = new Request.Builder().url(url).build();
			mWebSocket = mOkHttpClient.newWebSocket(request,
					new WebSocketListener() {

						@Override
						public void onClosed(WebSocket webSocket, int code,
								String reason) {
							// TODO Auto-generated method stub
							super.onClosed(webSocket, code, reason);
							System.out.println("onClosed");
						}

						@Override
						public void onClosing(WebSocket webSocket, int code,
								String reason) {
							// TODO Auto-generated method stub
							super.onClosing(webSocket, code, reason);

						}

						@Override
						public void onFailure(WebSocket webSocket, Throwable t,
								Response response) {
							// TODO Auto-generated method stub
							super.onFailure(webSocket, t, response);
							System.out.println("onFailure");
						}

						@Override
						public void onMessage(WebSocket webSocket, String text) {
							// TODO Auto-generated method stub
							super.onMessage(webSocket, text);
							try {
								System.out.println(text);
								JSONObject jo = new JSONObject(text);
								if (jo.getString("user") != null
										&& jo.getString("type").equals(
												"message")
										&& jo.getString("text")
												.equalsIgnoreCase("Print")) {
									final String channel = jo
											.getString("channel");

									// new Thread(){
									// public void run() {
									// send(channel, "begin to print");
									// };
									// }.start();
									executorService.execute(new Runnable() {

										@Override
										public void run() {
											// TODO
											// Auto-generated
											// method stub
											System.out.println("print"
													+ channel
													+ " begin to print 1");
											 send(channel, "begin to print");
										}
									});
								}
							} catch (Exception e) {
								// TODO: handle exception
							}

						}

						@Override
						public void onOpen(WebSocket webSocket,
								Response response) {
							// TODO Auto-generated method stub
							super.onOpen(webSocket, response);
							System.out.println("onOpen");
						}

					});

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("start thread");
	}

	private static void trustAllHosts(WebSocketClient appClient) {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };
		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			appClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(
					sc));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage() {
		Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
				"172.25.75.2", 8080));
		SlackApi api = new SlackApi(
//				"https://hooks.slack.com/services/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",
				"https://hooks.slack.com/services/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",
				proxy);
		// SlackApi api = new SlackApi(getService());
		api.call(new SlackMessage("my message"));
	}

	public String getRTM() {
		HttpURLConnection connection = null;
		try {
			// Create connection
			final URL url = new URL(
					"https://slack.com/api/rtm.start?token=xoxb-163906966384-EmuBPOcC8OplzMMdW2a50jtW&pretty=1");
			// final URL url = new URL("https://www.baidu.com");
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
					"172.25.75.2", 8080));
			connection = (HttpURLConnection) url.openConnection(proxy);
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(10000);
			connection.setUseCaches(false);
			connection.setDoInput(true);

			final InputStream is = connection.getInputStream();
			final BufferedReader rd = new BufferedReader(new InputStreamReader(
					is));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			System.out.println(response.toString());
			return response.toString();
		} catch (Exception e) {
			throw new SlackException(e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public String getService() {
		Gson gson = new Gson();
		String response = getRTM();
		JSONObject jo = null;
		String url = null;
		try {
			jo = new JSONObject(response);
			url = jo.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println(url);
		String replace = url.replace("wss", "ws");
		System.out.println(replace);
		return url;
	}
}
