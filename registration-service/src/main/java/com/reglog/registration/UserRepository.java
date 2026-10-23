package com.reglog.registration;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<UserAccount,Long> { boolean existsByEmailIgnoreCase(String email); boolean existsByMobile(String mobile); }
