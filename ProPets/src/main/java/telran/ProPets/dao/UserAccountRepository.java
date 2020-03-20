package telran.ProPets.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import telran.ProPets.model.UserAccount;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {

}
