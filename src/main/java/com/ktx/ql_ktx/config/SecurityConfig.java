package com.ktx.ql_ktx.config;

import com.ktx.ql_ktx.security.JwtAuthFilter;
import com.ktx.ql_ktx.security.NguoiDungDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Cau hinh phan quyen theo 3 vai tro:
 * - /api/sinh-vien/**        : chi SINH_VIEN
 * - /api/quan-ly-toa-nha/**  : QUAN_LY_TOA_NHA hoac BAN_QUAN_LY
 * - /api/ban-quan-ly/**      : chi BAN_QUAN_LY
 * - /api/auth/**             : cong khai (dang nhap, dang ky)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private NguoiDungDetailsService nguoiDungDetailsService;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(nguoiDungDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/ban-quan-ly/**").hasRole("BAN_QUAN_LY")
                .requestMatchers("/api/quan-ly-toa-nha/**").hasAnyRole("QUAN_LY_TOA_NHA", "BAN_QUAN_LY")
                .requestMatchers("/api/sinh-vien/**").hasAnyRole("SINH_VIEN", "QUAN_LY_TOA_NHA", "BAN_QUAN_LY")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}