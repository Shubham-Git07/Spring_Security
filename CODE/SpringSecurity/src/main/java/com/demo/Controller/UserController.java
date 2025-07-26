package com.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.demo.Entity.Users;
import com.demo.Service.UserService;

@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/registerUser")
	public Users registerUser(@RequestBody Users user) {
		return userService.registerUser(user);
	}

	@PostMapping("/login")
	public String login(@RequestBody Users user) {
		System.out.println(user);
		return userService.verify(user);
	}

}
