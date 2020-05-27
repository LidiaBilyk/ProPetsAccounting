package telran.ProPets.security.filter;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import telran.ProPets.configuration.AccountingConfiguration;

import java.io.IOException;
import java.security.Key;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;


@Service
@Order(20)
public class JwtFilter implements Filter {
	
	@Autowired
	AccountingConfiguration accountingConfiguration;
	
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
			UriTemplate template = new UriTemplate(accountingConfiguration.getTemplate());		
			String pathLogin = template.match(request.getRequestURI()).get("login");			
			if (!(path.matches(".+/role/.+") || path.matches(".+/block/.+"))) {
				if (!login.equals(pathLogin)) {					
					response.sendError(403, "Access denied");
					return;
				}
			}
			String jwt = createJwt(login);
			response.addHeader("X-Token", jwt);	
			response.addHeader("X-Login", login);
			chain.doFilter(new WrapperRequest(request, login), response);
			return;
		}				
		chain.doFilter(request, response);
	}
	
	public String createJwt(String login) {		
		
		SignatureAlgorithm signatureAlgotithm = SignatureAlgorithm.HS256;
		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		Instant expiration = issuedAt.plus(accountingConfiguration.getTerm(), ChronoUnit.DAYS);
		byte[] keySecret = DatatypeConverter.parseBase64Binary(accountingConfiguration.getSecret());
		Key signingKey = new SecretKeySpec(keySecret, signatureAlgotithm.getJcaName());
		JwtBuilder jwtBuilder = Jwts.builder()
				.setIssuedAt(Date.from(issuedAt))
				.setSubject(login)
				.setExpiration(Date.from(expiration))
				.signWith(signatureAlgotithm, signingKey);	
	
		return jwtBuilder.compact();
	}
	
	public Claims verifyJwt(String jwt) {
		Claims claims = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary(accountingConfiguration.getSecret()))
				.parseClaimsJws(jwt).getBody();										
		return claims;
	}
	
	private boolean checkPointCut(String path, String method) {
		boolean check = path.matches(".*/v1") && "Post".equalsIgnoreCase(method);
		check = check || path.matches(".*/login") || path.matches(".*/token/validation") || path.matches(".+/favorit.+") || path.matches(".+/activit.+");
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
