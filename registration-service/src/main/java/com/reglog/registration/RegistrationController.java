package com.reglog.registration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
@RestController @RequestMapping("/api/registration") @CrossOrigin(origins="${REGLOG_ORIGIN:http://localhost:5173}")
public class RegistrationController {
 private final UserRepository users; private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);
 public RegistrationController(UserRepository users){this.users=users;}
 public record Registration(@NotBlank @Size(min=3,max=100) String fullName,@NotBlank @Email @Size(max=254) String email,@NotBlank @Pattern(regexp="[0-9]{10}") String mobile,@NotBlank @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}$") String password) {}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
 public Map<String,String> register(@Valid @RequestBody Registration input){String name=input.fullName().trim();if(name.length()<3||name.length()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Full name must be 3 to 100 characters");if(users.existsByEmailIgnoreCase(input.email()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already registered");if(users.existsByMobile(input.mobile()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Mobile number is already registered");users.save(new UserAccount(name,input.email().trim(),input.mobile(),encoder.encode(input.password())));return Map.of("message","Account created");}
}


