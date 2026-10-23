package com.reglog.registration;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="users", uniqueConstraints={@UniqueConstraint(columnNames="email"),@UniqueConstraint(columnNames="mobile")})
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=100) public String fullName;
 @Column(nullable=false,length=254) public String email;
 @Column(nullable=false,length=10) public String mobile;
 @Column(nullable=false,length=100) public String passwordHash;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 protected UserAccount() {}
 public UserAccount(String name,String email,String mobile,String hash){this.fullName=name;this.email=email.toLowerCase();this.mobile=mobile;this.passwordHash=hash;}
}
