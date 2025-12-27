package it.unisa.resolveIt.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Permettiamo solo login, registrazione e risorse statiche
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                        .requestMatchers("/gestore").hasAuthority("GESTORE")
                        .requestMatchers("/home", "/my-profile").hasAnyAuthority("CLIENTE", "OPERATORE")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

                            // Reindirizzamento basato sui ruoli
                            if (roles.contains("GESTORE")) {
                                response.sendRedirect("/gestore");
                            } else if (roles.contains("OPERATORE")) {
                                response.sendRedirect("/ticket/operatore-home");
                            } else {
                                response.sendRedirect("/ticket/home");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        // Se un utente prova ad accedere a una pagina senza permessi (es. Cliente su /gestore)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            if (auth != null) {
                                Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
                                if (roles.contains("GESTORE")) {
                                    response.sendRedirect("/gestore");
                                } else {
                                    response.sendRedirect("/home");
                                }
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                );

        return http.build();
    }


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico");
    }
}