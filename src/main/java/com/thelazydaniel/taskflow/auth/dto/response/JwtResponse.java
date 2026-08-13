package com.thelazydaniel.taskflow.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class JwtResponse{
    private String accessToken;
    private String refreshToken;
    private String type = "bearer";
    private String username;
    private List<String> roles;
}
