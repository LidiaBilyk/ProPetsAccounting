package propets.accounting.service;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import propets.accounting.dto.UserProfileDto;
import propets.accounting.dto.UserRegisterDto;
import propets.accounting.dto.UserUpdateDto;

public interface UserAccountService {
	ResponseEntity<UserProfileDto> registerUser(UserRegisterDto userRegisterDto);
	UserProfileDto userLogin(String login);
	UserProfileDto getUserById(String login);
	UserProfileDto updateUser(String login, UserUpdateDto userUpdateDto);
	UserProfileDto deleteUser(String login);
	Set<String> addRole(String userLogin, String role);
	Set<String> removeRole(String userLogin, String role);
	boolean blockUser(String login, boolean block);
	ResponseEntity<String> checkJwt(String token);
	void addFavorite(String login, String serviceName, String favorite);
	void removeFavorite(String login, String serviceName, String favorite);
	Map<String, Set<String>> getUserFavorites(String login);
}
