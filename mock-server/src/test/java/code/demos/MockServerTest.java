package code.demos;

import static org.mockserver.integration.ClientAndServer.*;
import static org.mockserver.matchers.Times.*;
import static org.mockserver.model.HttpClassCallback.*;
import static org.mockserver.model.HttpForward.*;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.StringBody.*;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpForward;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;

public class MockServerTest {

	private static ClientAndServer mockServer;

	private static MockServerClient mockServerClient;

	@BeforeClass
	public static void startServer() {
		mockServer = startClientAndServer(7878);
		mockServerClient = new MockServerClient("127.0.0.1",7878);
		mockServerClient.when(
			request("/hello").withHeader("x-ncp-apigw-api-key","XDFFAG")
		).respond(
			response().withStatusCode(200)
				.withBody("world")
		);
	}

	@AfterClass
	public static void stopServer() {
		mockServer.stop();
	}

	private void createExpectationForInvalidAuth() {
		new MockServerClient("127.0.0.1", 1080).when(request().withMethod("POST")
				.withPath("/validate")
				.withHeader("\"Content-type\", \"application/json\"")
				.withBody(exact("{username: 'foo', password: 'bar'}")), exactly(1))
			.respond(response().withStatusCode(401)
				.withHeaders(new Header("Content-Type", "application/json; charset=utf-8"), new Header("Cache-Control", "public, max-age=86400"))
				.withBody("{ message: 'incorrect username and password combination' }")
				.withDelay(TimeUnit.SECONDS, 1));
	}

	@Test
	public void createExpectationForForward() {
		new MockServerClient("127.0.0.1", 1080)
			.when(
				request()
					.withMethod("GET")
					.withPath("/index.html"),
				exactly(1))
			.forward(
				forward()
					.withHost("www.mock-server.com")
					.withPort(80)
					.withScheme(HttpForward.Scheme.HTTP)
			);
	}

	private void createExpectationForCallBack() {
		mockServer
			.when(
				request().withPath("/callback"))
			.callback(
				callback()
					.withCallbackClass("com.baeldung.mock.server.TestExpectationCallback")
			);
	}

	public HttpResponse handle(HttpRequest httpRequest) {
		if (httpRequest.getPath().getValue().endsWith("/callback")) {
			return httpResponse;
		} else {
			return notFoundResponse();
		}
	}

	private void verifyPostRequest() {
		new MockServerClient("localhost", 1080).verify(
			request()
				.withMethod("POST")
				.withPath("/validate")
				.withBody(exact("{username: 'foo', password: 'bar'}")),
			VerificationTimes.exactly(1)
		);
	}

	public static HttpResponse httpResponse = response()
		.withStatusCode(200);
}