package com.example.demo.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
	private String authenticationToken;
	private String refreshToken;
	private Instant expiresAt;
	private String username;
}
