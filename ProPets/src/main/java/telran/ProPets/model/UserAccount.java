package telran.ProPets.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

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
@Entity
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
	@ElementCollection(fetch = FetchType.EAGER)
	Set<String> roles;
	@Singular
	@ElementCollection(fetch = FetchType.EAGER)
	List<String> favorites;
	boolean block;
	
	
	public boolean addRole(String role) {
		return roles.add(role);
		
	}
	
	public boolean removeRole(String role) {
		return roles.remove(role);
	}

	public boolean addFavorite(String favorite) {
		return favorites.add(favorite);
		
	}
	
	public boolean removeFavorite(String favorite) {
		return favorites.remove(favorite);
	}	
}
