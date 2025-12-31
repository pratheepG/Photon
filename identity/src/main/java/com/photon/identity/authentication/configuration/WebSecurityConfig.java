package com.photon.identity.authentication.configuration;

import com.photon.identity.authentication.handler.verificationhandler.impl.*;
import com.photon.identity.authentication.service.JwtUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity()
public class WebSecurityConfig {

    @Bean
    StaticPasswordAuthProvider staticPasswordAuthProvider(){
        return new StaticPasswordAuthProvider(passwordEncoder());
    }

    @Bean
    SmsOTPAuthProvider smsOTPAuthProvider(){
        return new SmsOTPAuthProvider(passwordEncoder());
    }

    @Bean
    EmailOTPAuthProvider emailOTPAuthProvider(){
        return new EmailOTPAuthProvider(passwordEncoder());
    }

    @Bean
    GoogleOAuthProvider googleOAuthProvider(){
        return new GoogleOAuthProvider();
    }

    @Bean
    PhotonOAuthProvider photonOAuthProvider(){
        return new PhotonOAuthProvider();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return new JwtUserDetailsService();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {
        return new ProviderManager(smsOTPAuthProvider(), emailOTPAuthProvider(), staticPasswordAuthProvider());
    }

//    @Bean
//    DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//
//        authProvider.setUserDetailsService(userDetailsService());
//        authProvider.setPasswordEncoder(passwordEncoder());
//
//        return authProvider;
//    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, AuthenticationManager auth) throws Exception {

    	return httpSecurity
                .cors(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(amr -> amr
						.requestMatchers("/authentication/login").permitAll()
						.requestMatchers("/authentication/register").permitAll()
                        .requestMatchers("/authentication/init").permitAll()
                        .requestMatchers("/certs/**").permitAll()
                        .requestMatchers("/auth-type/**").permitAll()
                        .requestMatchers("/identity-meta").permitAll()
                        .requestMatchers("/role/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/identity-provider/**").permitAll()
                        .requestMatchers("/sca-configuration/**").permitAll()
                        .requestMatchers("/mfa-condition/**").permitAll()
                        .requestMatchers("/user-device/**").permitAll()
                        .requestMatchers("/onboarding/**").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
						.anyRequest().permitAll())
				.sessionManagement(sessionMgmt -> sessionMgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

//    @Bean
//    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder, ApplicationConfigProperties applicationConfigProperties) {
//        UserDetails user1 = User.builder()
//                .username(applicationConfigProperties.getClientId())
//                .password(passwordEncoder.encode(applicationConfigProperties.getClientSecret()))
//                .roles("PHOTON_USER")
//                .build();
//
//        UserDetails user2 = User.builder()
//                .username(applicationConfigProperties.getPhotonClientId())
//                .password(passwordEncoder.encode(applicationConfigProperties.getPhotonClientSecret()))
//                .roles("PHOTON_GLOBAL_USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(user1, user2);
//    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

}