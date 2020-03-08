//package telran.ProPets.configuration;
//
//import java.util.Base64;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.springframework.context.annotation.Configuration;
//
//import telran.ProPets.exceptions.UserAuthentificationException;
//import telran.ProPets.model.UserAccount;
//import telran.ProPets.service.UserAccountCredentials;
//
//@Configuration
//public class AccountConfiguration {
//	Map<String, UserAccount> users = new ConcurrentHashMap<>();
//	
//	public UserAccountCredentials tokenDecode(String token) {		
//		try {
//			int pos = token.indexOf(" ");
//			token = token.substring(pos + 1);
//			String credential = new String(Base64.getDecoder().decode(token));
//			String[] credentials = credential.split(":");
//			return new UserAccountCredentials(credentials[0], credentials[1]);
//		} catch (Exception e) {
//			throw new UserAuthentificationException();
//		}
//	}	
//	
//	public boolean addUser(String sessionId, UserAccount userAccount) {
//		return users.put(sessionId, userAccount) == null;		
//	}
//	
//	public UserAccount getUser(String sessionId) {
//		return users.get(sessionId);
//	}
//}
