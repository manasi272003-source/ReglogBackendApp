package com.reglog.auth;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="auth_sessions",indexes=@Index(name="idx_sessions_user",columnList="user_id"))
public class AuthSession {
 @Id @Column(length=36) public String id;
 @Column(name="user_id",nullable=false) public Long userId;
 @Column(nullable=false) public Instant loginAt;
 @Column(nullable=false) public Instant expiresAt;
 @Column(nullable=false) public boolean revoked=false;
 protected AuthSession(){}
 public AuthSession(String id,Long userId,Instant expires){this.id=id;this.userId=userId;this.loginAt=Instant.now();this.expiresAt=expires;}
}


