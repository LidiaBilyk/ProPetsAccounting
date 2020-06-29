package propets.accounting.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import propets.accounting.configuration.AccountingConfiguration;
import propets.accounting.dao.UserAccountRepository;
import propets.accounting.dto.UserProfileDto;
import propets.accounting.dto.UserRegisterDto;
import propets.accounting.dto.UserUpdateDto;
import propets.accounting.exceptions.BadRequestException;
import propets.accounting.exceptions.ConflictException;
import propets.accounting.exceptions.ForbiddenException;
import propets.accounting.exceptions.NotFoundException;
import propets.accounting.model.UserAccount;

@Service
public class UserAccountServiceImpl implements UserAccountService {

	@Autowired
	AccountingConfiguration accountingConfiguration;

	@Autowired
	UserAccountRepository userAccountRepository;

	@Override
	public ResponseEntity<UserProfileDto> registerUser(UserRegisterDto userRegisterDto) {
		if (userAccountRepository.existsById(userRegisterDto.getEmail())) {
			throw new ConflictException();
		}
		String avatar = accountingConfiguration.getAvatarUri();
		String hashPassword = BCrypt.hashpw(userRegisterDto.getPassword(), BCrypt.gensalt());
		UserAccount userAccount = UserAccount.builder()
				.email(userRegisterDto.getEmail())
				.password(hashPassword)
				.name(userRegisterDto.getName())
				.role("User").avatar(avatar)
				.build();
		UserProfileDto userProfileDto = userAccountToUserProfileDto(userAccountRepository.save(userAccount));
		String jwt = createJwt(userRegisterDto.getEmail(), accountingConfiguration.getSecret());
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Token", jwt);
		return new ResponseEntity<UserProfileDto>(userProfileDto, headers, HttpStatus.OK);
	}

	private UserProfileDto userAccountToUserProfileDto(UserAccount userAccount) {
		return UserProfileDto.builder().email(userAccount.getEmail()).name(userAccount.getName())
				.phone(userAccount.getPhone()).avatar(userAccount.getAvatar()).roles(userAccount.getRoles()).build();
	}

	@Override
	public UserProfileDto userLogin(String login) {
		UserAccount userAccount = userAccountRepository.findById(login).get();
		if (userAccount.isBlock()) {
			throw new ForbiddenException();
		}
		return userAccountToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto getUserById(String login) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
		return userAccountToUserProfileDto(userAccount);
	}

	@Override
	public UserProfileDto updateUser(String login, UserUpdateDto userUpdateDto) {
		UserAccount userAccount = userAccountRepository.findById(login).get();			
		if (userUpdateDto.getName() != null) {
			userAccount.setName(userUpdateDto.getName());			
		}
		if (userUpdateDto.getPhone() != null) {
			userAccount.setPhone(userUpdateDto.getPhone());
		}
		if (userUpdateDto.getAvatar() != null) {
			userAccount.setAvatar(userUpdateDto.getAvatar());			
		}
		userAccountRepository.save(userAccount);
		if (userUpdateDto.getAvatar() != null || userUpdateDto.getName() != null) {
			userUpdateDto.setLogin(login);
			sendUserUpdateDtoToService(userUpdateDto);
		}
		return userAccountToUserProfileDto(userAccount);
	}

	private void sendUserUpdateDtoToService(UserUpdateDto userUpdateDto) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = null;		
		try {
			RequestEntity<UserUpdateDto> requestEntity = new RequestEntity<UserUpdateDto>(userUpdateDto, HttpMethod.PUT,
					new URI(accountingConfiguration.getUpdateUserData()));
			responseEntity = restTemplate.exchange(requestEntity, String.class);
		} catch (RestClientException e) {
			throw new ConflictException();
		} catch (URISyntaxException e) {
			throw new BadRequestException();
		}
	}

	@Override
	public UserProfileDto deleteUser(String login) {
		UserAccount userAccount = userAccountRepository.findById(login).get();
		userAccountRepository.deleteById(login);
		return userAccountToUserProfileDto(userAccount);
	}

	@Override
	public Set<String> addRole(String userLogin, String role) {
		UserAccount userAccount = userAccountRepository.findById(userLogin).orElseThrow(NotFoundException::new);
		userAccount.addRole(role);
		userAccountRepository.save(userAccount);
		return userAccount.getRoles();
	}

	@Override
	public Set<String> removeRole(String userLogin, String role) {
		UserAccount userAccount = userAccountRepository.findById(userLogin).orElseThrow(NotFoundException::new);
		userAccount.removeRole(role);
		userAccountRepository.save(userAccount);
		return userAccount.getRoles();
	}

	@Override
	public boolean blockUser(String userLogin, boolean block) {
		UserAccount userAccount = userAccountRepository.findById(userLogin).orElseThrow(NotFoundException::new);
		if (block) {
			userAccount.setBlock(true);
		} else {
			userAccount.setBlock(false);
		}
		userAccountRepository.save(userAccount);
		return userAccount.isBlock();
	}

	@Override
	public ResponseEntity<String> checkJwt(String token) {
		Claims claims = null;
		try {
			claims = verifyJwt(token, accountingConfiguration.getSecret());
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		UserAccount userAccount = userAccountRepository.findById(claims.getSubject()).orElse(null);
		if (userAccount == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String jwt = createJwt(claims.getSubject(), accountingConfiguration.getSecret());
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Token", jwt);
		headers.add("X-Login", userAccount.getEmail());
		return new ResponseEntity<>(headers, HttpStatus.OK);
	}

	public String createJwt(String login, String secret) {
		SignatureAlgorithm signatureAlgotithm = SignatureAlgorithm.HS256;
		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		Instant expiration = issuedAt.plus(accountingConfiguration.getTerm(), ChronoUnit.DAYS);
		byte[] keySecret = DatatypeConverter.parseBase64Binary(secret);
		Key signingKey = new SecretKeySpec(keySecret, signatureAlgotithm.getJcaName());
		JwtBuilder jwtBuilder = Jwts.builder()
				.setIssuedAt(Date.from(issuedAt))
				.setSubject(login)
				.setExpiration(Date.from(expiration))
				.signWith(signatureAlgotithm, signingKey);
		return jwtBuilder.compact();
	}

	public Claims verifyJwt(String jwt, String secret) {
		Claims claims = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary(secret))
				.parseClaimsJws(jwt)
				.getBody();
		return claims;
	}

	@Override
	public void addFavorite(String login, String serviceName, String favorite) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
		userAccount.addFavorite(serviceName, favorite);
		userAccountRepository.save(userAccount);
	}

	@Override
	public void removeFavorite(String login, String serviceName, String favorite) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
		userAccount.removeFavorite(serviceName, favorite);
		userAccountRepository.save(userAccount);
	}

	@Override
	public Map<String, Set<String>> getUserFavorites(String login) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
		return userAccount.getFavorites();
	}
}
