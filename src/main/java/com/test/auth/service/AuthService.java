package com.test.auth.service;

import com.test.auth.dto.request.LoginRequest;
import com.test.auth.dto.request.SignUpRequest;
import com.test.auth.dto.response.JwtResponse;
import com.test.auth.dto.response.MessageResponse;
import com.test.auth.dto.response.UserDetailsReponse;
import com.test.auth.model.ERole;
import com.test.auth.model.Role;
import com.test.auth.model.User;
import com.test.auth.repository.RoleRepository;
import com.test.auth.repository.UserRepository;
import com.test.auth.security.jwt.JwtUtils;
import com.test.auth.security.service.UserDetailsImplement;
import com.test.auth.security.service.UserDetailsServiceImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImplement userDetailsService;


    public ResponseEntity<?> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateToken(authentication);
        UserDetailsImplement userDetailsImplement = (UserDetailsImplement) authentication.getPrincipal();
        List<String> roles = userDetailsImplement.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return ResponseEntity.ok(new JwtResponse(token, userDetailsImplement.getId(), userDetailsImplement.getUsername(), userDetailsImplement.getEmail(), roles));
    }

    public ResponseEntity<?> signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Error: Username is alreday taken")
            );
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(
                    new MessageResponse("Error: Email is alreday use")
            );
        }
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        Set<String> request_roles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        if (request_roles == null) {
            Role role = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Role is not found"));
            roles.add(role);
        } else {
            request_roles.forEach(role -> {
                if ("admin".equals(role)) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Role is not found"));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Role is not found"));
                    roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User created successfully"));
    }

    public ResponseEntity<?> verifyToken(String token, HttpServletRequest request) {
        try {
            if (authenticationWithTokenUser(token, request)){
                UserDetailsImplement user = (UserDetailsImplement) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                UserDetailsReponse userDetailsReponse = new UserDetailsReponse();
                userDetailsReponse.setId(user.getId());
                userDetailsReponse.setEmail(user.getEmail());
                userDetailsReponse.setUsername(user.getUsername());
                userDetailsReponse.setRoles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                return ResponseEntity.ok(userDetailsReponse);
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Invalid Token"));

    }

    public boolean authenticationWithTokenUser(String token, HttpServletRequest request) {
        if (token != null && jwtUtils.validateJwtToken(token)) {
            String username = jwtUtils.getUserNameFromJwtToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        }
        return false;
    }
}
