package com.reglog.auth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.HexFormat;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AccountRepository users;private final SessionRepository sessions;private final RecoveryRepository recoveries;private final BCryptPasswordEncoder passwords;private final JwtService jwt;private final OtpMailer mailer;private final int otpMinutes;
 public AuthController(AccountRepository users,SessionRepository sessions,RecoveryRepository recoveries,BCryptPasswordEncoder passwords,JwtService jwt,OtpMailer mailer,@org.springframework.beans.factory.annotation.Value("${reglog.otp.minutes:10}") int otpMinutes){this.users=users;this.sessions=sessions;this.recoveries=recoveries;this.passwords=passwords;this.jwt=jwt;this.mailer=mailer;this.otpMinutes=otpMinutes;}
 public record Login(@NotBlank String identifier,@NotBlank String password){} public record PasswordChange(@NotBlank String currentPassword,@NotBlank @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}$") String newPassword){} public record Forgot(@NotBlank String identifier){} public record Verify(@NotBlank String recoveryToken,@NotBlank @Pattern(regexp="[0-9]{6}") String otp){} public record Reset(@NotBlank String resetToken,@NotBlank @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}$") String newPassword){}
 @PostMapping("/login") public Map<String,Object> login(@Valid @RequestBody Login in){Account u=users.findByEmailIgnoreCaseOrMobile(in.identifier().trim(),in.identifier().trim()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));if(!passwords.matches(in.password(),u.passwordHash))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");String id=UUID.randomUUID().toString();AuthSession session=sessions.save(new AuthSession(id,u.id,jwt.expiry()));return Map.of("token",jwt.issue(u.id,id),"user",user(u));}
 @GetMapping("/me") public Map<String,Object> me(Authentication a){return user(users.findById((Long)a.getPrincipal()).orElseThrow());}
 @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(Authentication a){Object id=a.getDetails();sessions.findById((String)id).ifPresent(s->{s.revoked=true;sessions.save(s);});}
 @PostMapping("/change-password") @Transactional public Map<String,String> change(Authentication a,@Valid @RequestBody PasswordChange in){Account u=users.findById((Long)a.getPrincipal()).orElseThrow();if(!passwords.matches(in.currentPassword(),u.passwordHash))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Current password is incorrect");u.passwordHash=passwords.encode(in.newPassword());users.save(u);revokeAll(u.id);return Map.of("message","Password changed; sign in again");}
 @PostMapping("/forgot-password") public Map<String,String> forgot(@Valid @RequestBody Forgot in){String challenge=UUID.randomUUID()+UUID.randomUUID().toString().replace("-","");String otp=String.format("%06d",new SecureRandom().nextInt(1_000_000));Account u=users.findByEmailIgnoreCaseOrMobile(in.identifier().trim(),in.identifier().trim()).orElse(null);RecoveryChallenge row=new RecoveryChallenge(challenge,u==null?null:u.id,passwords.encode(otp),Instant.now().plusSeconds(otpMinutes*60L));recoveries.save(row);if(u!=null)mailer.send(u.email,otp);return Map.of("recoveryToken",challenge,"message","If that account exists, a verification code has been sent.");}
 @PostMapping("/verify-otp") @Transactional public Map<String,String> verify(@Valid @RequestBody Verify in){RecoveryChallenge row=recoveries.lockById(in.recoveryToken()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code"));if(row.consumed||row.verified||!row.expiresAt.isAfter(Instant.now())||row.attempts>=5)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");row.attempts++;if(row.userId==null||!passwords.matches(in.otp(),row.otpHash)){recoveries.save(row);throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired code");}row.verified=true;recoveries.save(row);return Map.of("resetToken",row.id);}
 @PostMapping("/reset-password") @Transactional public Map<String,String> reset(@Valid @RequestBody Reset in){RecoveryChallenge r=recoveries.lockById(in.resetToken()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired recovery session"));if(!r.verified||r.consumed||!r.expiresAt.isAfter(Instant.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid or expired recovery session");Account u=users.findById(r.userId).orElseThrow();u.passwordHash=passwords.encode(in.newPassword());users.save(u);r.consumed=true;recoveries.save(r);revokeAll(u.id);return Map.of("message","Password reset; sign in again");}
 private void revokeAll(Long user){var active=sessions.findAllByUserIdAndRevokedFalse(user);active.forEach(s->s.revoked=true);sessions.saveAll(active);}
 private Map<String,Object> user(Account u){return Map.of("id",u.id,"fullName",u.fullName,"email",u.email,"mobile",u.mobile);}
}





