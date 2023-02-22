package code.demos;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HttpClientSyncComponent {


	@Value("${url:127.0.0.1:7878}")
	private String url;

	@Value("${apiKey:XDFFAG}")
	private String apiKey;

	private CloseableHttpClient httpClient;

	private Integer page = 0;

	private Boolean lastPage = false;

	@Autowired
	public void setHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;
	}



	public String hello() {
		String result = "error";
		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			URIBuilder uriBuilder = new URIBuilder("http://127.0.0.1:7878/hello");
			result = this.doGet(uriBuilder);
			stopWatch.stop();
			String urlBuildStr = uriBuilder.toString();
			log.info("apiUrl : {} , spend :{} times", urlBuildStr, stopWatch.getTime());
		} catch (Exception ex) {
			log.error("request hello occurred exception", ex);
		}
		return result;
	}


	private URIBuilder makeDefaultUriBuilder() throws Exception {
		URIBuilder uriBuilder = new URIBuilder(url);
		uriBuilder.addParameter("includeProductData", "true");
		uriBuilder.addParameter("pageSize", "100");
		uriBuilder.addParameter("sort", "eventTime,ASC");
		uriBuilder.addParameter("actionResultType", "SUCCESS");
		return uriBuilder;
	}


	private String doGet(URIBuilder uriBuilder) {
		String content = "";
		CloseableHttpResponse response = null;
		try {
			HttpGet get = new HttpGet(uriBuilder.build());
			get.setHeader("x-ncp-apigw-api-key", apiKey);
			response = httpClient.execute(get);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				log.error("Execute request with status {}", statusCode);
			} else {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					content = EntityUtils.toString(entity, Consts.UTF_8);
				}
			}
		} catch (Exception e) {
			log.error("Fail to execute request , url : {} ", url);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (Exception e) {
			}
		}
		return content;
	}
}
