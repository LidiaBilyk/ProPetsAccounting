package telran.ProPets.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import telran.ProPets.dto.UserProfileDto;
import telran.ProPets.dto.UserRegisterDto;
import telran.ProPets.dto.UserRegisterResponseDto;

public interface UserAccountService {
	ResponseEntity<UserRegisterResponseDto> registerUser(UserRegisterDto userRegisterDto);
	UserProfileDto userLogin(String login);
	UserProfileDto getUserById(String login);
	UserProfileDto updateUser(String login, UserProfileDto userProfileDto);
	void userLogout(String login);
	UserProfileDto deleteUser(String login);
	Set<String> addRole(String userLogin, String role);
	Set<String> removeRole(String userLogin, String role);
	boolean blockUser(String login, boolean block);
	ResponseEntity<String> checkJwt(String token);
	List<String> addFavorite(String userLogin, String favorite);
	List<String> removeFavorite(String userLogin, String favorite);
	List<String> getUserFavorite(String userLogin);

}
