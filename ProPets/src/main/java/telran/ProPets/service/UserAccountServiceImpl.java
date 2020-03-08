package telran.ProPets.service;

import java.security.Key;
import java.util.Date;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import telran.ProPets.dao.UserAccountRepository;
import telran.ProPets.dto.UserProfileDto;
import telran.ProPets.dto.UserRegisterDto;
import telran.ProPets.dto.UserRegisterResponseDto;
import telran.ProPets.exceptions.ConflictException;
import telran.ProPets.exceptions.ForbiddenException;
import telran.ProPets.exceptions.NotFoundException;
import telran.ProPets.model.UserAccount;

@Service
public class UserAccountServiceImpl implements UserAccountService {
	
	String secret = "123_Password";	

	@Autowired
	UserAccountRepository userAccountRepository;

	@Override
	public UserRegisterResponseDto registerUser(UserRegisterDto userRegisterDto) {
		if (userAccountRepository.existsById(userRegisterDto.getEmail())) {
			throw new ConflictException();
		}
		String hashPassword = BCrypt.hashpw(userRegisterDto.getPassword(), BCrypt.gensalt());
		UserAccount userAccount = UserAccount.builder()
				.email(userRegisterDto.getEmail())
				.password(hashPassword)
				.name(userRegisterDto.getName())
				.role("User")
				.avatar("https://www.gravatar.com/avatar/0?d=mp")
				.build();
		return userAccountToUserRegisterResponceDto(userAccountRepository.save(userAccount));
	}

	private UserRegisterResponseDto userAccountToUserRegisterResponceDto(UserAccount userAccount) {
		return UserRegisterResponseDto.builder()
				.email(userAccount.getEmail())
				.name(userAccount.getName())
				.avatar(userAccount.getAvatar())
				.roles(userAccount.getRoles())
				.build();
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
		UserAccount userAccount = userAccountRepository.findById(login).get();
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
		userAccountRepository.save(userAccount);
		return userAccountToUserProfileDto(userAccount);
	}

	@Override
	public void userLogout(String login) {
		// TODO Auto-generated method stub

	}

	@Transactional
	@Override
	public UserProfileDto deleteUser(String login) {
		UserAccount userAccount = userAccountRepository.findById(login).get();
		userAccountRepository.deleteById(login);
		return userAccountToUserProfileDto(userAccount);
	}

	@Override
	public List<String> addRole(String userLogin, String role) {
		UserAccount userAccount = userAccountRepository.findById(userLogin).orElseThrow(NotFoundException::new);
		userAccount.addRole(role);		
		userAccountRepository.save(userAccount);
		return userAccount.getRoles();
	}

	@Override
	public List<String> removeRole(String userLogin, String role) {
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
		claims = verifyJwt(token, secret);
		} catch (Exception e) {			
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}			
		UserAccount userAccount = userAccountRepository.findById(claims.getSubject()).orElse(null);		
		if (userAccount == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		String jwt = createJwt(claims.getSubject(), secret);
		HttpHeaders headers = new HttpHeaders();
		headers.add("X-Token", jwt);
		headers.add("X-Avatar", userAccount.getAvatar());
		headers.add("X-UserName", userAccount.getName());
		return new ResponseEntity<>(headers, HttpStatus.OK);
	}

	public String createJwt(String login, String secret) {
		long term = 900000;
		SignatureAlgorithm signatureAlgotithm = SignatureAlgorithm.HS256;
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		long expMillis = nowMillis + term;
		Date exp = new Date(expMillis);
		byte[] keySecret = DatatypeConverter.parseBase64Binary(secret);
		Key signingKey = new SecretKeySpec(keySecret, signatureAlgotithm.getJcaName());
		JwtBuilder jwtBuilder = Jwts.builder()
				.setIssuedAt(now)
				.setSubject(login)
				.setExpiration(exp)
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
	
}
