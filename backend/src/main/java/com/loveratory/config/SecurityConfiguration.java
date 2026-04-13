package com.loveratory.config;

import com.loveratory.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全設定。
 * 設定 JWT 無狀態認證、URL 存取規則與密碼編碼器。
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, BootstrapAdminProperties.class})
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 設定 Security Filter Chain。
     * 停用 CSRF、啟用無狀態 Session、設定 URL 存取權限與 JWT 過濾器。
     *
     * @param httpSecurity HttpSecurity 設定物件
     * @return SecurityFilterChain
     * @throws Exception 設定例外
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/invitations/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/invitations/*/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/invitations/*/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("SYSTEM_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * 密碼編碼器。
     * 使用 BCrypt 演算法進行密碼雜湊。
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
