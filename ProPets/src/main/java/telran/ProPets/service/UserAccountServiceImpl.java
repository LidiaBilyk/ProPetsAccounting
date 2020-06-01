package telran.ProPets.service;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import telran.ProPets.configuration.AccountingConfiguration;
import telran.ProPets.dao.UserAccountRepository;
import telran.ProPets.dto.UserProfileDto;
import telran.ProPets.dto.UserRegisterDto;
import telran.ProPets.exceptions.ConflictException;
import telran.ProPets.exceptions.ForbiddenException;
import telran.ProPets.exceptions.NotFoundException;
import telran.ProPets.model.UserAccount;

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
				.role("User")
				.avatar(avatar)
				.build();
		UserProfileDto userProfileDto = userAccountToUserProfileDto(userAccountRepository.save(userAccount));
		String jwt = createJwt(userRegisterDto.getEmail(), accountingConfiguration.getSecret());
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Token", jwt);
		return new ResponseEntity<UserProfileDto>(userProfileDto, headers, HttpStatus.OK);		 
	}


	private UserProfileDto userAccountToUserProfileDto(UserAccount userAccount) {
		return UserProfileDto.builder()
				.email(userAccount.getEmail())
				.name(userAccount.getName())
				.phone(userAccount.getPhone())
				.avatar(userAccount.getAvatar())
				.roles(userAccount.getRoles())
				.build();
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
	public UserProfileDto updateUser(String login, UserProfileDto userProfileDto) {
		UserAccount userAccount = userAccountRepository.findById(login).get();

		if (userProfileDto.getName() != null) {
			userAccount.setName(userProfileDto.getName());
		}
		if (userProfileDto.getPhone() != null) {
			userAccount.setPhone(userProfileDto.getPhone());
		}
		if (userProfileDto.getAvatar() != null) {
			userAccount.setAvatar(userProfileDto.getAvatar());
		}
		userAccountRepository.save(userAccount);
		return userAccountToUserProfileDto(userAccount);
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

//	@Override
//	public Map<String, Set<String>> getUserFavorite(String login) {
//		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
//		return userAccount.getFavorites();
//	}

	@Override
	public void addActivity(String login, String serviceName, String activity) {		
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);		
		userAccount.addActivity(serviceName, activity);			
		userAccountRepository.save(userAccount);		
	}


	@Override
	public void removeActivity(String login, String serviceName, String activity) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
		userAccount.removeActivity(serviceName, activity);		
		userAccountRepository.save(userAccount);		
	}

//	@Override
//	public Map<String, Set<String>> getUserActivity(String login) {
//		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
//		return userAccount.getActivities();
//	}	
	
	@Override
	public Map<String, Set<String>> getUserData(String login, boolean dataType) {
		UserAccount userAccount = userAccountRepository.findById(login).orElseThrow(NotFoundException::new);
		return dataType? userAccount.getFavorites() : userAccount.getActivities();
	}
	
}
