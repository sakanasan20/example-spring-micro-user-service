package tw.niq.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import feign.Logger;
import tw.niq.example.client.FeignErrorDecoder;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class ExampleSpringMicroUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleSpringMicroUserServiceApplication.class, args);
	}
	
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public HttpExchangeRepository httpExchangeRepository() {
		return new InMemoryHttpExchangeRepository();
	}
	
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.build();
	}
	
	@Bean
	Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}
	
	@Bean
	public FeignErrorDecoder feignErrorDecoder() {
		return new FeignErrorDecoder();
	}

}
