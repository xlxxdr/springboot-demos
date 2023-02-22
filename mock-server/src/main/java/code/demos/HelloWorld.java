package code.demos;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorld {

	@RequestMapping("/")
	public String helloWorld() {

		return "Hello World ";

	}

	@RequestMapping("/login")
	public String login(@RequestParam(value = "name", required = true) String name, @RequestParam(value = "password", required = true) String pwd) {

		return name + " " + pwd;

	}

}
