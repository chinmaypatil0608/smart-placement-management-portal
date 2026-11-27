package com.example.placement;
import com.example.placement.model.UserAccount; import com.example.placement.repository.UserAccountRepository; import org.springframework.beans.factory.annotation.Value; import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.crypto.password.PasswordEncoder;
@Configuration public class AdminSeeder {
 @Bean CommandLineRunner seedAdmin(UserAccountRepository users,PasswordEncoder encoder,@Value("${app.admin.email:}") String email,@Value("${app.admin.password:}") String password){return args->{if(email!=null&&!email.isBlank()&&password!=null&&!password.isBlank()&&!users.existsByEmailIgnoreCase(email)){users.save(new UserAccount(email.trim().toLowerCase(),encoder.encode(password),"ADMIN",null));}};}
}
