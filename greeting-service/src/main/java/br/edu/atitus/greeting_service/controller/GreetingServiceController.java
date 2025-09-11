package br.edu.atitus.greeting_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.greeting_service.entity.User;

@RestController
@RequestMapping("/greeting")
public class GreetingServiceController {

	@Value("${greeting-service.greeting}")
	private String greeting;

	@Value("${greeting-service.default-name}")
	private String defaultName;

	@GetMapping("/{name}")
	public String greet(@PathVariable String name) {
		return greeting + ", " + name +"!!!";
	}
	
	@PostMapping
	public String greet(@RequestBody User user) {
		return greeting + ", " + user.getName() +"!!!";
	}
}
