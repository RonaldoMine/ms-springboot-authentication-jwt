package com.test.auth.controller;

import com.test.auth.security.service.UserDetailsImplement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/test/")
public class TestController {

    @GetMapping("public")
    public String publicAccess() {
        return "Public access";
    }

    @GetMapping("user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String userAccess() {
        UserDetailsImplement userDetails = (UserDetailsImplement) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "User access with username "+userDetails.getEmail();
    }

    @GetMapping("admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Admin access";
    }
}
