package telran.ProPets.security.filter;

import java.io.IOException;
import java.security.Key;
import java.security.Principal;
import java.util.Base64;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import telran.ProPets.dao.UserAccountRepository;
import telran.ProPets.exceptions.UserAuthentificationException;
import telran.ProPets.model.UserAccount;
import telran.ProPets.service.UserAccountCredentials;

@Service
@Order(10)
public class AuthenticationFilter implements Filter {

	@Autowired
	UserAccountRepository repository;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();		
		String auth = request.getHeader("Authorization");		
		
		if (path.matches("/\\w*/account/v1/login")) {			
			UserAccountCredentials credentials = null;
			try {
				credentials = tokenDecode(auth);
			} catch (Exception e) {
				response.sendError(401, "Header Authorization is not valid");
				return;
			}
			UserAccount userAccount = repository.findById(credentials.getLogin()).orElse(null);
			if (userAccount == null) {
				response.sendError(401, "User not found");
				return;
			}
			if (!BCrypt.checkpw(credentials.getPassword(), userAccount.getPassword())) {
				response.sendError(403, "Password incorrect");
				return;
			}
			response.addHeader("X-Token", createJwt(credentials.getLogin()));

			chain.doFilter(new WrapperRequest(request, credentials.getLogin()), response);
			return;
		}
		chain.doFilter(request, response);
	}

	private UserAccountCredentials tokenDecode(String token) {
		try {
			int pos = token.indexOf(" ");
			token = token.substring(pos + 1);
			String credential = new String(Base64.getDecoder().decode(token));
			String[] credentials = credential.split(":");
			return new UserAccountCredentials(credentials[0], credentials[1]);
		} catch (Exception e) {
			throw new UserAuthentificationException();
		}
	}

	public String createJwt(String login) {
		long term = 900000;
		String secret = "123_Password";
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
				.signWith(signatureAlgotithm,
				signingKey);

		return jwtBuilder.compact();
	}

//	private boolean checkPointCut(String path, String method) {
//		boolean check = path.matches("/\\w*/account/v1") && "Post".equalsIgnoreCase(method);
//		check = check || path.startsWith("/h2");
//		return check;
//	}

	private class WrapperRequest extends HttpServletRequestWrapper {

		String user;

		public WrapperRequest(HttpServletRequest request, String user) {
			super(request);
			this.user = user;
		}

		@Override
		public Principal getUserPrincipal() {
			return new Principal() { // or return () -> user;

				@Override
				public String getName() {
					return user;
				}
			};
		}
	}
}
