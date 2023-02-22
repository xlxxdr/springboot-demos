package code.demos;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringDemoApplication.class)
public class HttpClientSyncComponentTest extends MockServerTest {

	@Autowired
	private HttpClientSyncComponent httpClientSyncComponent;


	@Test
	public void test_query() {
		String result = httpClientSyncComponent.hello();
		assertEquals("world",result);
	}



}