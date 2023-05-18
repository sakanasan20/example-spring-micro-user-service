package tw.niq.example.controller;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
		return "Working on port " + environment.getProperty("local.server.port");
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
	
}
