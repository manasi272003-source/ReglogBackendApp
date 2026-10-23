package com.reglog.auth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SessionRepository extends JpaRepository<AuthSession,String>{Optional<AuthSession> findByIdAndRevokedFalse(String id);List<AuthSession> findAllByUserIdAndRevokedFalse(Long userId);}
