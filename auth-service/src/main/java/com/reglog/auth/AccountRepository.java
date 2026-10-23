package com.reglog.auth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface AccountRepository extends JpaRepository<Account,Long>{Optional<Account> findByEmailIgnoreCase(String email);Optional<Account> findByMobile(String mobile);Optional<Account> findByEmailIgnoreCaseOrMobile(String email,String mobile);}
