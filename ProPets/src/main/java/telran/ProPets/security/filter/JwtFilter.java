package telran.ProPets.security.filter;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.IOException;
import java.security.Key;
import java.security.Principal;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(20)
public class JwtFilter implements Filter {
	
	String secret = "123_Password";
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();
		String method = request.getMethod();
		String auth = request.getHeader("X-Token");		
		
		if (!checkPointCut(path, method)) {			
			Claims claims = null;
			try {				
			claims = verifyJwt(auth);
			} catch (Exception e) {				
				response.sendError(401, "Header X-Token is not valid");
				return;
			}			
			String login = claims.getSubject();
			String jwt = createJwt(login);
			response.addHeader("X-Token", jwt);				
			chain.doFilter(new WrapperRequest(request, login), response);
			return;
		}				
		chain.doFilter(request, response);
	}
	
	public String createJwt(String login) {
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
	
	public Claims verifyJwt(String jwt) {
		Claims claims = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary(secret))
				.parseClaimsJws(jwt).getBody();										
		return claims;
	}
	
	private boolean checkPointCut(String path, String method) {
		boolean check = path.matches("/\\w*/account/v1") && "Post".equalsIgnoreCase(method);
		check = check || path.startsWith("/h2") || path.matches("/\\w*/account/v1/login") || path.matches(".*/token/validation");
		return check;
	}
	
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
