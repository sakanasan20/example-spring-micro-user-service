package tw.niq.example.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import tw.niq.example.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurity {
	
	private final Logger logger = LoggerFactory.getLogger(WebSecurity.class);
	
	private final Environment environment;
	
	private final UserService userService;
	
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public WebSecurity(Environment environment, UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.environment = environment;
		this.userService = userService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		
		String remoteConfig = environment.getProperty("tw.niq.example.config.remote.active");
		String localConfig = environment.getProperty("tw.niq.example.config.local.active");
		
		logger.info("## Remote Config: " + remoteConfig);
		logger.info("## Local Config: " + localConfig);
	}

	@Bean
	protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
		
		// Configure AuthenticationManagerBuilder
		AuthenticationManagerBuilder authenticationManagerBuilder = 
				http.getSharedObject(AuthenticationManagerBuilder.class);
		
		authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
		
		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
		
		AuthenticationFilter authenticationFilter = 
				new AuthenticationFilter(environment, userService, authenticationManager);
		
		authenticationFilter.setFilterProcessesUrl(environment.getProperty("tw.niq.example.login.url"));
		
		AuthorizationFilter authorizationFilter = new AuthorizationFilter(authenticationManager, environment);
		
		http.csrf().disable();
		
		http.authorizeHttpRequests()
			.requestMatchers("/api/v1/users/**")
				.access(new WebExpressionAuthorizationManager("hasIpAddress('" + environment.getProperty("tw.niq.example.gateway.ip") + "')"))
			.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
			.requestMatchers(HttpMethod.POST, environment.getProperty("tw.niq.example.login.url")).permitAll()
			.requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
			.and()
			.addFilter(authenticationFilter)
			.addFilter(authorizationFilter)
			.authenticationManager(authenticationManager)
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		http.headers().frameOptions().disable();
		
		return http.build();
	}
	
}
