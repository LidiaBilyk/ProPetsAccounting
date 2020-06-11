package propets.accounting.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import propets.accounting.model.UserAccount;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {

}
