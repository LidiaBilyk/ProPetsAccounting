package telran.ProPets;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import telran.ProPets.dao.UserAccountRepository;
import telran.ProPets.model.UserAccount;

@SpringBootApplication
public class ProPetsAccountingApplication implements CommandLineRunner{
	
	@Autowired
	UserAccountRepository userAccountRepository;

	public static void main(String[] args) {
		SpringApplication.run(ProPetsAccountingApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (!userAccountRepository.existsById("admin")) {
			String avatar = "https://www.gravatar.com/avatar/0?d=mp";
			String hashPassword = BCrypt.hashpw("admin", BCrypt.gensalt());
			UserAccount admin = UserAccount.builder()
					.email("admin")
					.password(hashPassword)
					.name("admin")
					.avatar(avatar)
					.role("User")
					.role("Moderator")
					.role("Administrator")					
					.build();
			userAccountRepository.save(admin);
		}
		
	}
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**");
			}
		};
		
	}

}
