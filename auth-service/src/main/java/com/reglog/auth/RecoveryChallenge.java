package com.reglog.auth;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="password_recovery")
public class RecoveryChallenge {
 @Id @Column(length=64) public String id;
 @Column public Long userId;
 @Column(nullable=false,length=64) public String otpHash;
 @Column(nullable=false) public Instant expiresAt;
 @Column(nullable=false) public int attempts=0;
 @Column(nullable=false) public boolean verified=false;
 @Column(nullable=false) public boolean consumed=false;
 protected RecoveryChallenge(){}
 public RecoveryChallenge(String id,Long userId,String otpHash,Instant expires){this.id=id;this.userId=userId;this.otpHash=otpHash;this.expiresAt=expires;}
}

