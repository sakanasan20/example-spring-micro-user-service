package tw.niq.example.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends BasicAuthenticationFilter {
	
	private final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);
	
	private Environment environment;

	public AuthorizationFilter(AuthenticationManager authenticationManager, Environment environment) {
		super(authenticationManager);
		this.environment = environment;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String authorizationHeader = request.getHeader(environment.getProperty("tw.niq.example.authorization.token.header.name"));
		
		if (authorizationHeader == null ||
				!authorizationHeader.startsWith(environment.getProperty("tw.niq.example.authorization.token.header.prefix"))) {
			chain.doFilter(request, response);
			return;
		}
		
		UsernamePasswordAuthenticationToken authentization = getAuthentication(request);
		
		SecurityContextHolder.getContext().setAuthentication(authentization);
		chain.doFilter(request, response);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		
		String authorizationHeader = request.getHeader(environment.getProperty("tw.niq.example.authorization.token.header.name"));
		
		if (authorizationHeader == null) {
			return null;
		}
		
		String token = authorizationHeader.replace(environment.getProperty("tw.niq.example.authorization.token.header.prefix"), "");
		String tokenSecret = environment.getProperty("tw.niq.example.token.secret");
		
		if (tokenSecret == null) {
			return null;
		}
		
		byte[] secretKeyBytes = Base64.getEncoder().encode(tokenSecret.getBytes());
		SecretKey secretKey = new SecretKeySpec(secretKeyBytes, SignatureAlgorithm.HS512.getJcaName());
		
		JwtParser jwtParser = Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build();
		
		Jwt<Header, Claims> jwt = jwtParser.parse(token);
		String userId = jwt.getBody().getSubject();
		
		if (userId == null) {
			return null;
		}
		
		logger.info("AuthorizationFilter Done");
		
		return new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
	}

}
