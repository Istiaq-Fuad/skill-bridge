package org.jobai.skillbridge.config;

import org.jobai.skillbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private CorsConfig corsConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http
                .getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/", "/api/users/register", "/api/users/login", "/api/users/logout",
                                "/api/users/profile")
                        .permitAll()
                        .requestMatchers("/api/jobs", "/api/jobs/{id}", "/api/jobs/keyword/**")
                        .permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/users", "/api/users/{id}", "/api/users/username/{username}")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{id}")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{id}")
                        .hasRole("ADMIN")

                        // Employer-specific endpoints
                        .requestMatchers("/api/employer/**")
                        .hasRole("EMPLOYER")
                        .requestMatchers("/api/employers/**")
                        .hasRole("EMPLOYER")
                        .requestMatchers("/api/intelligent-jobs/**")
                        .hasRole("EMPLOYER")
                        .requestMatchers(HttpMethod.POST, "/api/jobs")
                        .hasRole("EMPLOYER")

                        // Job seeker-specific endpoints
                        .requestMatchers("/api/job-seekers/**")
                        .hasRole("JOB_SEEKER")

                        // Mixed access endpoints (require authentication)
                        .requestMatchers("/api/advanced-matching/**")
                        .authenticated()
                        .requestMatchers("/api/applications/**")
                        .authenticated()
                        .requestMatchers("/api/profiles/**")
                        .authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
