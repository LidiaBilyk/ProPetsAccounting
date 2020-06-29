package propets.accounting.controller;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import propets.accounting.dto.UserProfileDto;
import propets.accounting.dto.UserRegisterDto;
import propets.accounting.dto.UserUpdateDto;
import propets.accounting.service.UserAccountService;

@RestController
@RequestMapping("/{lang}/v1")
public class UserAccountController {

	@Autowired
	UserAccountService userAccountService;

	@PostMapping
	public ResponseEntity<UserProfileDto> registerUser(@RequestBody UserRegisterDto userRegisterDto) {
		return userAccountService.registerUser(userRegisterDto);
	}

	@PostMapping("/login")
	public UserProfileDto userLogin(Principal principal) {
		return userAccountService.userLogin(principal.getName());
	}

	@GetMapping("/{login:.*}/info")
	public UserProfileDto getUserById(@PathVariable String login) {
		return userAccountService.getUserById(login);
	}

	@PutMapping("/{login:.*}")
	public UserProfileDto updateUser(@PathVariable String login, @RequestBody UserUpdateDto userProfileDto) {
		return userAccountService.updateUser(login, userProfileDto);
	}

	@DeleteMapping("/{login:.*}")
	public UserProfileDto deleteUser(@PathVariable String login) {
		return userAccountService.deleteUser(login);
	}

	@PutMapping("/{login:.*}/role/{role}")
	public Set<String> addRole(@PathVariable String login, @PathVariable String role) {
		return userAccountService.addRole(login, role);
	}

	@DeleteMapping("/{login:.*}/role/{role}")
	public Set<String> removeRole(@PathVariable String login, @PathVariable String role) {
		return userAccountService.removeRole(login, role);
	}

	@PutMapping("/{login:.*}/block/{block}")
	public boolean blockUser(@PathVariable String login, @PathVariable boolean block) {
		return userAccountService.blockUser(login, block);
	}

	@GetMapping("/token/validation")
	public ResponseEntity<String> tokenValidation(@RequestHeader("X-Token") String token) {
		return userAccountService.checkJwt(token);
	}

	@PutMapping("/{login:.*}/favorite/{favorite}")
	public void addFavorite(@PathVariable String login, @RequestHeader("X-ServiceName") String serviceName,	@PathVariable String favorite) {
		userAccountService.addFavorite(login, serviceName, favorite);
	}

	@DeleteMapping("/{login:.*}/favorite/{favorite}")
	public void removeFavorite(@PathVariable String login, @RequestHeader("X-ServiceName") String serviceName, @PathVariable String favorite) {
		userAccountService.removeFavorite(login, serviceName, favorite);
	}

	@GetMapping("/{login:.*}/userdata")
	public Map<String, Set<String>> getUserFavorites(@PathVariable String login) {
		return userAccountService.getUserFavorites(login);
	}

}
