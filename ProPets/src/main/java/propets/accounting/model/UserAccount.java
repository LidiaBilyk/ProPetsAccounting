package propets.accounting.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(of = {"email"})
@Document(collection = "users")
public class UserAccount implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	String email;
	String password;
	String name;
	String avatar;
	String phone;
	@Singular
	Set<String> roles;	
	@Builder.Default
	Map<String, Set<String>> favorites = new HashMap<>();
	@Builder.Default
	Map<String, Set<String>> activities = new HashMap<>();
	boolean block;
	
	public boolean addRole(String role) {
		return roles.add(role);		
	}
	
	public boolean removeRole(String role) {
		return roles.remove(role);
	}

	public boolean addFavorite(String serviceName, String favorite) {
		return favorites.computeIfAbsent(serviceName, k -> new HashSet<>()).add(favorite);		
	}
	
	public boolean removeFavorite(String serviceName, String favorite) {
		return favorites.get(serviceName).remove(favorite);
	}	
	
	public boolean addActivity(String serviceName, String activity) {
		return activities.computeIfAbsent(serviceName, k -> new HashSet<>()).add(activity);		
	}
	
	public boolean removeActivity(String serviceName, String activity) {
		return activities.get(serviceName).remove(activity);
	}
}
