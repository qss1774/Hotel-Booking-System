package com.example.HotelBooking.security;

import com.example.HotelBooking.exceptions.CustomAccessDenialHandler;
import com.example.HotelBooking.exceptions.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @Configuration：表明这是一个配置类
 *
 * @EnableWebSecurity：启用 Spring Security 的基本功能
 *
 * @EnableMethodSecurity：允许在方法上用注解（如 @PreAuthorize）做权限控制
 *
 * @RequiredArgsConstructor：自动注入 final 的字段（你的 AuthFilter、CustomAccessDenialHandler 等）
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityFilter {

    /**
     * AuthFilter：前面写的 JWT 验证过滤器
     *
     * CustomAccessDenialHandler：用户权限不足时的处理器
     *
     * CustomAuthenticationEntryPoint：用户未登录时的处理器
     */
    private final AuthFilter authFilter;

    private final CustomAccessDenialHandler customAccessDenialHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * 步骤 | 描述
     * 1️⃣ | 关闭 CSRF、开启 CORS
     * 2️⃣ | 配置未登录/权限不足的处理方式
     * 3️⃣ | 允许公开访问 /api/auth/** 等接口
     * 4️⃣ | 限制其他接口必须认证
     * 5️⃣ | 禁用 session，只靠 JWT
     * 6️⃣ | 加上自定义的 AuthFilter 过滤每次请求
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

//        禁用 CSRF（因为 JWT 是无状态的，不需要 CSRF 保护）
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
//                开启跨域请求支持（默认设置
                .cors(Customizer.withDefaults())

// 权限不足 (403) 由 customAccessDenialHandler 处理
//未登录 (401) 由 customAuthenticationEntryPoint 处理
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(customAccessDenialHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/api/auth/**", "/api/rooms/**", "api/bookings/**").permitAll()
                        .anyRequest().authenticated()
//                        /api/auth/**, /api/rooms/**, /api/bookings/** 这些接口不需要登录（公开访问）
//其他所有接口必须登录认证才能访问
                )
//                不保存 session，每次请求都要靠 JWT 来认证
//
//核心原因：JWT是无状态的，不依赖服务器存储用户登录状态
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
//        把你写的 AuthFilter 加到标准的 UsernamePasswordAuthenticationFilter 之前
//
//这样能在用户访问接口前，先解析和校验 JWT
        return httpSecurity.build();
    }

    /**
     * 提供 BCrypt 加密，用于加密和验证用户密码。
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用来进行用户名密码的认证（比如登录接口时用到）
     * @param authenticationConfiguration
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }


}
