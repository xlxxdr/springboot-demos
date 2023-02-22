package code.demos;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

	@Bean
	public CloseableHttpClient getHttpClient() {
		return createHttpClient();
	}

	public CloseableHttpClient createHttpClient() {
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setMaxTotal(200);
		connManager.setDefaultMaxPerRoute(200);
		RequestConfig config = RequestConfig.custom()
			.setConnectTimeout(10 * 1000)
			.setSocketTimeout(60 * 1000)
			.setConnectionRequestTimeout(10 * 1000)
			.build();

		ConnectionKeepAliveStrategy keepAliveStrategy = new DefaultConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response,
				HttpContext context) {
				long time = super.getKeepAliveDuration(response, context);
				int keepAlive = 5 * 60 * 1000;
				return time == -1 ? keepAlive : time;
			}
		};

		HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
			@Override
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				//Do not retry if over max retry count
				if (executionCount >= 3) {
					return false;
				}
				//Timeout
				if (exception instanceof InterruptedIOException) {
					return false;
				}
				//Unknown host
				if (exception instanceof UnknownHostException) {
					return false;
				}
				//Connection refused
				if (exception instanceof ConnectTimeoutException) {
					return false;
				}
				//SSL handshake exception
				if (exception instanceof SSLException) {
					return false;
				}

				HttpClientContext clientContext = HttpClientContext.adapt(context);
				HttpRequest request = clientContext.getRequest();
				boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
				if (idempotent) {
					return true;
				}
				return false;
			}
		};

		CloseableHttpClient httpClient = HttpClients.custom()
			.setConnectionManager(connManager)
			.setKeepAliveStrategy(keepAliveStrategy)
			.setDefaultRequestConfig(config)
			.setRetryHandler(myRetryHandler)
			.build();
		return httpClient;
	}
}
