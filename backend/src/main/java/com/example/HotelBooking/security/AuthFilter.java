package com.example.HotelBooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @Component：被 Spring 管理并注入到过滤器链中。
 *
 * OncePerRequestFilter：确保每个请求只执行一次这个过滤器。
 *
 * 拦截每个请求
 *
 * 从请求头中解析 Token
 *
 * 从 Token 获取邮箱 -> 加载用户
 *
 * 验证 Token 是否有效
 *
 * 注入登录状态
 *
 * 放行请求
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {

    /**
     * JwtUtils：用于解析和验证 Token。
     *
     * CustomUserDetailsService：从数据库中加载用户数据
     */
    private final JwtUtils jwtUtils;

    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {


        String token = getTokenFromRequest(request);

        if (token != null) {
//            从 Token 中提取邮箱，加载用户
            String email = jwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

//            创建一个 AuthenticationToken 实例，传入当前用户的权限。
//            将它存入 SecurityContextHolder 中，这样后面的 Controller 就可以获取到登录用户的信息了。

            if (StringUtils.hasText(email) && jwtUtils.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        try {
            filterChain.doFilter(request, response);
//            放行请求进入下一个过滤器（或最终的 Controller）
        } catch (Exception e) {
            log.error(e.getMessage());
        }


    }

    /**
     * 从 Authorization 请求头中获取 Bearer token
     *
     * 去掉前缀 “Bearer ” 后得到真正的 JWT
     *
     * @param request
     * @return
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String tokenWithBearer = request.getHeader("Authorization");
        if (tokenWithBearer != null && tokenWithBearer.startsWith("Bearer ")) {
            return tokenWithBearer.substring(7);
        }
        return null;
    }
}
