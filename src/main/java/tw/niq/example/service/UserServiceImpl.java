package tw.niq.example.service;

import java.util.ArrayList;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import tw.niq.example.client.AccountServiceClient;
import tw.niq.example.dto.UserDto;
import tw.niq.example.entity.UserEntity;
import tw.niq.example.model.AccountModel;
import tw.niq.example.repository.UserRepository;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	
	private final RestTemplate restTemplate;
	
	private final AccountServiceClient accountServiceClient;

	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
			RestTemplate restTemplate, AccountServiceClient accountServiceClient) {
		this.userRepository = userRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.restTemplate = restTemplate;
		this.accountServiceClient = accountServiceClient;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		UserEntity userEntity = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException(email));
		
		return new User(
				userEntity.getEmail(), 
				userEntity.getEncryptedPassword(), 
				true, 
				true, 
				true, 
				true, 
				new ArrayList<>());
	}

	@Override
	public UserDto createUser(UserDto userDtoToCreate) {

		ModelMapper modelMapper = new ModelMapper();
		
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserEntity userEntityToSave = modelMapper.map(userDtoToCreate, UserEntity.class);
		
		userEntityToSave.setUserId(UUID.randomUUID().toString());
		
		userEntityToSave.setEncryptedPassword(bCryptPasswordEncoder.encode(userDtoToCreate.getPassword()));
		
		UserEntity userEntitySaved = userRepository.save(userEntityToSave);
		
		UserDto userDtoCreated = modelMapper.map(userEntitySaved, UserDto.class);
		
		return userDtoCreated;
	}

	@Override
	public UserDto getUserByEmail(String email) {
		
		ModelMapper modelMapper = new ModelMapper();
		
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserEntity userEntityFound = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException(email));
		
		UserDto userDtoFound = modelMapper.map(userEntityFound, UserDto.class);
		
		return userDtoFound;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		
		ModelMapper modelMapper = new ModelMapper();
		
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserEntity userEntityFound = userRepository.findByUserId(userId)
				.orElseThrow(() -> new UsernameNotFoundException(userId));
		
		UserDto userDtoFound = modelMapper.map(userEntityFound, UserDto.class);
		
		// RestTemplate
//		String accountServiceUrl = String.format("http://accountservice/api/v1/accounts/%s", userId);
//		
//		ResponseEntity<AccountModel> accountResponse = 
//				restTemplate.exchange(accountServiceUrl, HttpMethod.GET, null, new ParameterizedTypeReference<AccountModel>(){});
//		
//		AccountModel accountRestTemplate = accountResponse.getBody();
//		
//		log.debug("accountRestTemplate: " + accountRestTemplate.toString());
		
		// FeignClient
		log.debug("Before calling account service");
		
		AccountModel accountFeignClient = accountServiceClient.getAccount(userId);
		
		log.debug("After calling account service");
		
		log.debug(accountFeignClient.toString());

		userDtoFound.setAccount(accountFeignClient);

		return userDtoFound;
	}

}
