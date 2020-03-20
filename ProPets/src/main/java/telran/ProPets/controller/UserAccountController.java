package telran.ProPets.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import telran.ProPets.dto.UserProfileDto;
import telran.ProPets.dto.UserRegisterDto;
import telran.ProPets.dto.UserRegisterResponseDto;
import telran.ProPets.service.UserAccountService;

@RestController
@CrossOrigin
@RequestMapping("/{lang}/v1")
public class UserAccountController {
	
	@Autowired
	UserAccountService userAccountService;
	
	@PostMapping
	public ResponseEntity<UserRegisterResponseDto> registerUser(@RequestBody UserRegisterDto userRegisterDto) {
		return userAccountService.registerUser(userRegisterDto);
	}

	@PostMapping("/login")
	public UserProfileDto userLogin(Principal principal) {
		return userAccountService.userLogin(principal.getName());
	}
	
	@GetMapping("/{login:.*}/info")
	public UserProfileDto getUserById(@PathVariable String login, @RequestHeader("X-token") String token) {		
		return userAccountService.getUserById(login);
	}
	
	@PutMapping
	public UserProfileDto updateUser(Principal principal, @RequestBody UserProfileDto userProfileDto, @RequestHeader("X-token") String token) {
		return userAccountService.updateUser(principal.getName(), userProfileDto);
	}
	
	@DeleteMapping
	public UserProfileDto deleteUser(Principal principal, @RequestHeader("X-token") String token) {
		return userAccountService.deleteUser(principal.getName());
	}
	
	@PutMapping("/{login:.*}/role/{role}")
	public Set<String> addRole(@PathVariable String login, @PathVariable String role, @RequestHeader("X-token") String token) {		
		return userAccountService.addRole(login, role);
	}
	
	@DeleteMapping("/{login:.*}/role/{role}")
	public Set<String> removeRole(@PathVariable String login, @PathVariable String role, @RequestHeader("X-token") String token) {		
		return userAccountService.removeRole(login, role);
	}
	
	@PutMapping("/{login:.*}/block/{block}")
	public boolean blockUser(@PathVariable String login, @PathVariable boolean block, @RequestHeader("X-token") String token) {
		return userAccountService.blockUser(login, block);
	}
	
	@GetMapping("/token/validation")
	public ResponseEntity<String> tokenValidation(@RequestHeader("X-Token") String token) {
		return userAccountService.checkJwt(token);		
	}
	
	@PutMapping("/favorite/{favorite}")
	public List<String> addFavorite(Principal principal, @PathVariable String favorite, @RequestHeader("X-Token") String token) {
		return userAccountService.addFavorite(principal.getName(), favorite);
	}
	
	@DeleteMapping("/favorite/{favorite}")
	public List<String> removeFavorite(Principal principal, @PathVariable String favorite, @RequestHeader("X-Token") String token) {
		return userAccountService.removeFavorite(principal.getName(), favorite);
	}
	
	@GetMapping("/favorites")
	public List<String> getUserFavorite(Principal principal, @RequestHeader("X-Token") String token) {
		return userAccountService.getUserFavorite(principal.getName());
	}
}
