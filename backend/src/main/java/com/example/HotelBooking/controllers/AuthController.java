package com.example.HotelBooking.controllers;

import com.example.HotelBooking.dtos.LoginRequest;
import com.example.HotelBooking.dtos.RegistrationRequest;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName:AuthController
 * Package:com.example.HotelBooking.controllers
 * Description:
 *
 * @date:2025-04-26 7:06 p.m.
 * @author:Qss
 */

/**
 * 前端发送POST请求
 *    ↓
 * Spring Boot拦截请求 → AuthController
 *    ↓
 * 调用 userService 中的方法（registerUser 或 loginUser）
 *    ↓
 * 返回一个 HTTP 200 OK 响应，带上Response对象（通常是JSON）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Response> registerUser(@RequestBody @Valid RegistrationRequest request) {
        return ResponseEntity.ok(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Response> loginUser(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }
}
