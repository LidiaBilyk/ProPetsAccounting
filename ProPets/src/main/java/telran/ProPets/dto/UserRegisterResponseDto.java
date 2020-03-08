package telran.ProPets.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegisterResponseDto {
	
	String email;	
	String name;
	String avatar;
	List<String> roles;

}
