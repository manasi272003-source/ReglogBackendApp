package com.reglog.auth;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface RecoveryRepository extends JpaRepository<RecoveryChallenge,String> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select r from RecoveryChallenge r where r.id = :id")
 Optional<RecoveryChallenge> lockById(@Param("id") String id);
}
