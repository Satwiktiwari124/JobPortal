package com.example.jobPortal.config;

import com.example.jobPortal.security.JwtAuthenticationFilter;
import com.example.jobPortal.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(auth -> auth

                        // Public authentication pages and static resources
                        .requestMatchers(
                                "/login.html",
                                "/register.html",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error",
                                "/auth/login",
                                "/auth/register",
                                "/auth/register/recruiter",
                                "/auth/logout"
                        ).permitAll()

                        // Public homepage
                        .requestMatchers("/", "/index.html")
                        .permitAll()

                        // Public job browsing
                        .requestMatchers(HttpMethod.GET, "/jobs", "/jobs/**")
                        .permitAll()

                        // Recruiters manage jobs
                        .requestMatchers(
                                HttpMethod.POST, "/jobs", "/jobs/**"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.PUT, "/jobs", "/jobs/**"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.PATCH, "/jobs", "/jobs/**"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.DELETE, "/jobs", "/jobs/**"
                        ).hasRole("RECRUITER")

                        // Authenticated candidate/recruiter profile
                        .requestMatchers("/users/me").authenticated()

                        // Candidate application features
                        .requestMatchers(
                                HttpMethod.POST, "/applications/jobs/**"
                        ).hasRole("USER")

                        .requestMatchers(
                                HttpMethod.GET, "/applications/my"
                        ).hasRole("USER")

                        // Recruiter applicant management
                        .requestMatchers(
                                HttpMethod.GET, "/applications/recruiter"
                        ).hasRole("RECRUITER")

                        .requestMatchers(
                                HttpMethod.PATCH, "/applications/*/status"
                        ).hasRole("RECRUITER")

                        // Candidate resume features
                        .requestMatchers(
                                HttpMethod.POST, "/resumes/upload"
                        ).hasRole("USER")

                        .requestMatchers(
                                HttpMethod.GET, "/resumes/my/download"
                        ).hasRole("USER")

                        .requestMatchers(
                                HttpMethod.GET, "/resumes/applications/**"
                        ).hasRole("RECRUITER")

                        // Recruiter dashboard
                        .requestMatchers("/recruiter.html")
                        .hasRole("RECRUITER")

                        // All other routes require authentication
                        .anyRequest().authenticated()
                )

                // Preserve custom 401 and 403 behaviour
                .exceptionHandling(exceptions -> exceptions

                        .authenticationEntryPoint((request, response, exception) -> {
                            String uri = request.getRequestURI();

                            if (uri.endsWith(".html") || "/".equals(uri)) {
                                response.sendRedirect("/login.html");
                            } else {
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized"
                                );
                            }
                        })

                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden"
                                )
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
