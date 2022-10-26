package com.test.auth.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    String username, password;
}
