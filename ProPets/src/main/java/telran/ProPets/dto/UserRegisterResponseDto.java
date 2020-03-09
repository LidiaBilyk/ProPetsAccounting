package telran.ProPets.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserRegisterResponseDto {
	
	String email;	
	String name;
	String avatar;
	Set<String> roles;

}
