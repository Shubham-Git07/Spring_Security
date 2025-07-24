package com.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.demo.Entity.UserPricipal;
import com.demo.Entity.Users;
import com.demo.Repository.UserRepo;

@Service
public class MyUserDetailsService implements UserDetailsService {

	// now fetch username and password from database
	
	@Autowired
	private UserRepo repo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("Trying to authenticate user: " + username);
		Users user = repo.findByUsername(username);

		if (user == null) {
			System.out.println("user not found");
			throw new UsernameNotFoundException("user not found");
		}
		return new UserPricipal(user);
	}

}
