package tw.niq.example.security;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tw.niq.example.dto.UserDto;
import tw.niq.example.model.LoginUserModel;
import tw.niq.example.service.UserService;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	private final Environment environment;
	
	private final UserService userService;
	
	public AuthenticationFilter(Environment environment, UserService userService, AuthenticationManager authenticationManager) {
		super(authenticationManager);
		this.environment = environment;
		this.userService = userService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		try {

			LoginUserModel creds = new ObjectMapper().readValue(request.getInputStream(), LoginUserModel.class);

			return getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(
							creds.getEmail(), 
							creds.getPassword(), 
							new ArrayList<>()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
			FilterChain chain, Authentication authResult) throws IOException, ServletException {
		
		String email = ((User) authResult.getPrincipal()).getUsername();
		
		UserDto userDto = userService.getUserByEmail(email);
		
		Instant now = Instant.now();
		
		String tokenSecret = environment.getProperty("tw.niq.example.token.secret");
		
		byte[] tokenSecretBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
		
		SecretKey secretKey = new SecretKeySpec(tokenSecretBytes, SignatureAlgorithm.HS512.getJcaName());
		
		String token = Jwts.builder()
			.setSubject(userDto.getUserId())
			.setExpiration(Date.from(now
					.plusMillis(Long.parseLong(environment.getProperty("tw.niq.example.token.expiration_time")))))
			.setIssuedAt(Date.from(now))
			.signWith(secretKey, SignatureAlgorithm.HS512)
			.compact();
		
		response.addHeader("token", token);
		response.addHeader("userId", userDto.getUserId());
	}

}
