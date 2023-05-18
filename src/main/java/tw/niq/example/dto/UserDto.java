package tw.niq.example.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserDto implements Serializable {

	private static final long serialVersionUID = -5724677234298879632L;

	private String userId;
	
	private String firstName;
	
	private String lastName;

	private String email;
	
	private String password;
	
	private String encryptedPassword;
	
}
