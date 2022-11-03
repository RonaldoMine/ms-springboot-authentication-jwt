package com.test.auth.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserDetailsReponse {
    Long id;
    String email;
    String username;
    List<String> roles;
}
