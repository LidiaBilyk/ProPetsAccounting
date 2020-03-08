package telran.ProPets.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import telran.ProPets.model.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {

}
