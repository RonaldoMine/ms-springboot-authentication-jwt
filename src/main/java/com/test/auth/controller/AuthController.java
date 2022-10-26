package com.test.auth.controller;

import com.test.auth.dto.request.LoginRequest;
import com.test.auth.dto.request.SignUpRequest;
import com.test.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/auth/")
@CrossOrigin(origins = "*")
@RestController
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("sign-up")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }
}
