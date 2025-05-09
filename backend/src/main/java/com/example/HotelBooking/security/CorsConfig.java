package com.example.HotelBooking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Configuration：告诉 Spring 这是一个配置类，启动时会加载。

因为前端如果跟后端 不是同源（比如端口不同，或者不同服务器），浏览器出于安全机制，会拦截请求。
所以后端必须明确告诉浏览器："这个请求是我允许的"，才能跨域访问。

比如：

前端 localhost:3000

后端 localhost:8080

需要 CORS 才能正常调用 API。

 */
@Configuration
public class CorsConfig {

    /**
     * 注册一个 WebMvcConfigurer Bean，它专门用来扩展 Spring MVC 的功能，比如 CORS 配置
     * @return
     */
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
//        允许所有接口支持跨域	Allow CORS for all endpoints
//        只允许这四种 HTTP 方法跨域	Only allow these HTTP methods
//        允许来自任何域名的请求	Allow requests from any origin (*)
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedOrigins("*");
            }
        };
    }
}
