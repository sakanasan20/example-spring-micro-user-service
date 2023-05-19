package tw.niq.example.service;

import java.util.ArrayList;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import tw.niq.example.dto.UserDto;
import tw.niq.example.entity.UserEntity;
import tw.niq.example.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.userRepository = userRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
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

}
