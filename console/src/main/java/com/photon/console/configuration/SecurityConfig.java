package com.photon.console.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.properties.ApplicationConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api-manager/**").permitAll()
                        .requestMatchers("/deployment/**").permitAll()
                        .requestMatchers("/", "/index.html", "/assets/**", "/server-console/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .httpBasic(httpBasic -> httpBasic
                        .realmName("Console"));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder, ApplicationConfigProperties applicationConfigProperties) {
        UserDetails user1 = User.builder()
                .username(applicationConfigProperties.getXApiKey())
                .password(passwordEncoder.encode(applicationConfigProperties.getXApiSecret()))
                .roles("PHOTON_USER")
                .build();

        UserDetails user2 = User.builder()
                .username(applicationConfigProperties.getCompositeXApiKey())
                .password(passwordEncoder.encode(applicationConfigProperties.getCompositeXApiSecret()))
                .roles("PHOTON_GLOBAL_USER")
                .build();

        return new InMemoryUserDetailsManager(user1, user2);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}