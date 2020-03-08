package telran.ProPets;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import telran.ProPets.dao.UserAccountRepository;
import telran.ProPets.model.UserAccount;

@SpringBootApplication
public class ProPetsApplication implements CommandLineRunner{
	
	@Autowired
	UserAccountRepository userAccountRepository;

	public static void main(String[] args) {
		SpringApplication.run(ProPetsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (!userAccountRepository.existsById("admin")) {
			String hashPassword = BCrypt.hashpw("admin", BCrypt.gensalt());
			UserAccount admin = UserAccount.builder()
					.email("admin")
					.password(hashPassword)
					.role("User")
					.role("Moderator")
					.role("Administrator")					
					.build();
			userAccountRepository.save(admin);
		}
		
	}

}
