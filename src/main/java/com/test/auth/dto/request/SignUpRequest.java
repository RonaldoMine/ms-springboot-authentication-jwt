package com.test.auth.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class SignUpRequest {

    @NotBlank
    String username;

    @Email
    @NotBlank
    String email;

    @Size(min = 6, max = 40)
    @NotBlank
    String password;

    Set<String> roles;
}
