package tw.niq.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import tw.niq.example.model.AccountModel;

@FeignClient(name = "accountservice")
public interface AccountServiceClient {

	@GetMapping("/api/v1/accounts/{userId}")
	@Retry(name = "accountservice")
	@CircuitBreaker(name = "accountservice", fallbackMethod = "getAccountFallback")
	public AccountModel getAccount(@PathVariable("userId") String userId);
	
	default AccountModel getAccountFallback(String userId, Throwable exception) {
		System.out.println("userId: " + userId);
		System.out.println(exception.getMessage());
		return new AccountModel();
	}
	
}
