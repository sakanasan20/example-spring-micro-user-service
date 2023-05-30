package tw.niq.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import tw.niq.example.model.AccountModel;

@FeignClient(name = "accountservice")
public interface AccountServiceClient {

	@GetMapping("/api/v1/accountss/{userId}")
	public AccountModel getAccount(@PathVariable("userId") String userId);
	
}
