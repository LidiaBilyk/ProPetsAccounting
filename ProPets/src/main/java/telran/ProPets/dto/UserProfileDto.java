package telran.ProPets.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class UserProfileDto {
	
	String email;	
	String name;
	String avatar;
	String phone;
	@Singular
	List<String> roles;
	
}
