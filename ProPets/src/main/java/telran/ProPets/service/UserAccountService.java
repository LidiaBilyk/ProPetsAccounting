package telran.ProPets.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;

import telran.ProPets.dto.RolesDto;
import telran.ProPets.dto.UserProfileDto;
import telran.ProPets.dto.UserRegisterDto;
import telran.ProPets.dto.UserRegisterResponseDto;

public interface UserAccountService {
	UserRegisterResponseDto registerUser(UserRegisterDto userRegisterDto);
	UserProfileDto userLogin(String login);
	UserProfileDto getUserById(String login);
	UserProfileDto updateUser(String login, UserProfileDto userProfileDto);
	void userLogout(String login);
	UserProfileDto deleteUser(String login);
	List<String> addRole(String userLogin, String role);
	List<String> removeRole(String userLogin, String role);
	boolean blockUser(String login, boolean block);
	ResponseEntity<String> checkJwt(String token);

}
