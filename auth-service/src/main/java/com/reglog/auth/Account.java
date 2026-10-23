package com.reglog.auth;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="users")
public class Account {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=100) public String fullName;
 @Column(nullable=false,unique=true,length=254) public String email;
 @Column(nullable=false,unique=true,length=10) public String mobile;
 @Column(nullable=false,length=100) public String passwordHash;
 @Column(nullable=false) public Instant createdAt;
 protected Account(){}
}
