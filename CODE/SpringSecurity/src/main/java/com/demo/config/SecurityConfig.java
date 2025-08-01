package com.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.User;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtFilter jwtFilter;

	// here we are using our own filterChain and disabling default spring security
	// filter chain

	@Bean // Tells Spring: call this method & manage the returned object
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		// to disable csrfToken
		Customizer<CsrfConfigurer<HttpSecurity>> csrfCust = new Customizer<CsrfConfigurer<HttpSecurity>>() {
			@Override
			public void customize(CsrfConfigurer<HttpSecurity> t) {
				t.disable();
			}
		};
		http.csrf(csrfCust);
		// http.csrf(Customizer -> Customizer.disable()); // lambda expression

		// login authorization
		Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> autho = new Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>() {
			@Override
			public void customize(
					AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry t) {
				t.requestMatchers("register", "login").permitAll().anyRequest().authenticated();
			}
		};
		http.authorizeHttpRequests(autho); // here we are authentication the user but where user will give username and
											// password so we need to use form login
//		http.authorizeHttpRequests(request -> request.anyRequest().authenticated()); // using lambda expression

//		http.formLogin(Customizer.withDefaults());

		http.httpBasic(Customizer.withDefaults()); // withDefaults() is static method in functional interface Customizer

		// now as we disabled csrf token then try to make http stateless
		// so that we should create new session id every time
		// create new session each time
		Customizer<SessionManagementConfigurer<HttpSecurity>> sessionManagementCustomizer = new Customizer<SessionManagementConfigurer<HttpSecurity>>() {
			@Override
			public void customize(SessionManagementConfigurer<HttpSecurity> t) {
				t.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
			}
		};
		http.sessionManagement(sessionManagementCustomizer);
//		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// after login we are again asking for username and password to access methods
		// like getAllStudents
		// which is not right way because after login we should be able to access all
		// resources
		// here we are adding jwtFilter before UsernamePasswordAuthenticationFilter so
		// it will validate token
		// if token validates then it will skip UPAF filter check
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	// now we want our username and password check from database for multiple users
	// here we have hard coded values
//	@Bean
//	public UserDetailsService userDetailsService() {
//		UserDetails user1 = User.withDefaultPasswordEncoder().username("kiran").password("k@123").roles("USER").build();
//
//		UserDetails user2 = User.withDefaultPasswordEncoder().username("vaibhav").password("v@123").roles("ADMIN")
//				.build();
//
//		return new InMemoryUserDetailsManager(user1, user2);
//	}

	// now deal with actual database
	@Bean
	public AuthenticationProvider authenticationProvider() {
		// there are many authentication provider
		// to deal with database we have DaoAuthenticationProvider
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//		provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance()); // here we are not using any password encoder
		provider.setPasswordEncoder(new BCryptPasswordEncoder(10)); // when user try to access the data we try to encode
																	// the password
		// now i want to customize UserDetailsService not use default one.
		provider.setUserDetailsService(userDetailsService); // tells to verify username and password from
															// userDetailsService
		// so here we provide reference of UserDetailsService
		// since it is interface we need to provide implementation of it
		// which we provided in MyUserDetailsService
		return provider;
	}

	// till now AuthenticationManager was handling AuthenticationProvider internally
	// now we want to handle AuthenticationManager by ourself
	// to provide our own implementation create a bean of AuthenticationManager
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager(); // we got a hold of authenticationManager
		// AuthenticationManager will talk to AuthenticationProvider
	}

}

// http.build() = builds and returns the SecurityFilterChain object which Spring Security uses internally 
// to protect HTTP requests.
