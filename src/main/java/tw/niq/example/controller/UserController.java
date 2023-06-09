package tw.niq.example.controller;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import tw.niq.example.dto.UserDto;
import tw.niq.example.model.CreateUserModel;
import tw.niq.example.model.UserModel;
import tw.niq.example.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;
	
	private final Environment environment;

	public UserController(UserService userService, Environment environment) {
		this.userService = userService;
		this.environment = environment;
	}

	@GetMapping("/status")
	public String status() {
		
		String port = environment.getProperty("local.server.port");
		
		return "Working on port " + port;
	}
	
	@PostMapping(
			consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, 
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<UserModel> createUser(@Valid @RequestBody CreateUserModel createUserModel) {
		
		log.debug(createUserModel.toString());
		
		ModelMapper modelMapper = new ModelMapper();
		
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserDto userDtoToCreate = modelMapper.map(createUserModel, UserDto.class);
		
		UserDto userDtoCreated = userService.createUser(userDtoToCreate);
		
		UserModel userModel = modelMapper.map(userDtoCreated, UserModel.class);
		
		return new ResponseEntity<UserModel>(userModel, HttpStatus.CREATED);
	}
	
//	@PreAuthorize("principal == #userId")
//	@PostAuthorize("principal == returnObject.body.getUserId()")
	@GetMapping(value = "/{userId}", 
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<UserModel> getUser(@PathVariable("userId") String userId) {
		
		log.debug(userId);
		
		ModelMapper modelMapper = new ModelMapper();
		
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserDto userDtoFound = userService.getUserByUserId(userId);
		
		UserModel userModel = modelMapper.map(userDtoFound, UserModel.class);
		
		return new ResponseEntity<UserModel>(userModel, HttpStatus.OK);
	}
	
}
