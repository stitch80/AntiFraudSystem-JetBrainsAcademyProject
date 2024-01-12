package antifraud.security;

import antifraud.data.AppUserRepository;
import antifraud.entity.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint restAuthenticationEntryPoint
    ) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/actuator/shutdown").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/api/auth/user/*").hasRole("ADMINISTRATOR")
                                .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT")
                                .requestMatchers(HttpMethod.PUT, "/api/auth/access", "/api/auth/role").hasRole("ADMINISTRATOR")
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT")
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/antifraud/suspicious-ip",
                                        "/api/antifraud/stolencard").hasRole("SUPPORT")
                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/antifraud/suspicious-ip/*",
                                        "/api/antifraud/stolencard/*").hasRole("SUPPORT")
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/antifraud/suspicious-ip",
                                        "/api/antifraud/stolencard").hasRole("SUPPORT")
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/history").hasRole("SUPPORT")
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/history/*").hasRole("SUPPORT")
                                .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole("SUPPORT")
                                .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
//                        .requestMatchers("/error").anonymous()
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(CsrfConfigurer::disable)                           // For modifying requests via Postman
//                .csrf(csrf -> csrf.disable())
                .exceptionHandling(handing -> handing
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                )
                .headers(headers -> headers.frameOptions().disable())           // for Postman, the H2 console
//                .authorizeHttpRequests(requests -> requests                     // manage access
//                                .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
//                                .requestMatchers("/actuator/shutdown").permitAll()      // needs to run test
//                         other matchers
//                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                // other configurations
                .build();
    }


    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepo) {
        return username -> {
            System.out.println("Getting UserDetails");
            AppUser user = userRepo
                    .findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
            System.out.println(user.getId() + " " + user.getUsername() + " " + user.getPassword());
            AppUserAdapter output = new AppUserAdapter(user);
            System.out.println(output.getAuthorities());
            return output;
        };
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
