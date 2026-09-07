package me.zinch.itmo.mts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import me.zinch.itmo.mts.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter)
                        throws Exception {
                return http
                                .csrf(csrf -> csrf
                                                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                                                .ignoringRequestMatchers("/api/auth/login"))
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers(
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**")
                                                .permitAll()
                                                .requestMatchers("/api/auth/login", "/api/auth/csrf").permitAll()
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }
}
