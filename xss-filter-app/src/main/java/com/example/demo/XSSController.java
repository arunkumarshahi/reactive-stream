package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class XSSController {

	@PostMapping("/xss")
	public String postInput(@RequestBody String val) {
		val = XSSUtils.stripXSS(val);
		return val;
	}
	
	@GetMapping("/xss/{val}")
	public String getInput(@PathVariable String val) {
		val = XSSUtils.stripXSS(val);
		return val;
	}
}
