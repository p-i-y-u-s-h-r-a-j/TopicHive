package com.example.demo.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AuthenticationResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.exception.SpringRedditException;
import com.example.demo.model.NotificationEmail;
import com.example.demo.model.User;
import com.example.demo.model.VerificationToken;

import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VerificationTokenRepository;
//import com.example.demo.security.JwtProvider;


import lombok.AllArgsConstructor;
@Service
@AllArgsConstructor
public class AuthService {


	private PasswordEncoder encoder;
	private final UserRepository repo;
	private final VerificationTokenRepository verificationTokenRepository;
	private final MailService mailService;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final RefreshTokenService refreshTokenService;
	@Transactional
	public void signup(RegisterRequest registerRequest) {
		User user=new User();
		user.setEmail(registerRequest.getEmail());
		user.setUsername(registerRequest.getUsername());
		user.setPassword(encoder.encode(registerRequest.getPassword()));
		user.setCreated(Instant.now());
		user.setEnabled(false);
		
		repo.save(user);
		
		String token=generateVerificationToken(user);
		mailService.sendMail(new NotificationEmail("Please Activate your Account",user.getEmail(),"Thank you for Signing Up for Reddit, please click on the link below to activate your account "+
		"http://localhost:8080/api/auth/accountVerification/"+token));
}

	@Transactional
	private String generateVerificationToken(User user) {
		String token=UUID.randomUUID().toString();
		VerificationToken verificationToken=new VerificationToken();
		verificationToken.setUser(user);
		verificationToken.setToken(token);
		
		verificationTokenRepository.save(verificationToken);
		return token;
	}

	public void verifyAccount(String token) {
		Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
		verificationToken.orElseThrow(()->new SpringRedditException("Invalid Token"));
		fetchUserAndEnable(verificationToken.get());
	}

	@Transactional
	private void fetchUserAndEnable(VerificationToken verificationToken) {
		String username=verificationToken.getUser().getUsername();
		User user=repo.findByUsername(username).orElseThrow(()->new SpringRedditException("User with given Username( "+username+" ) not found!"));
		user.setEnabled(true);
		repo.save(user);
	}

	public AuthenticationResponse login(LoginRequest loginRequest) {
		Authentication authenticate=authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), 
						loginRequest.getPassword()
						)
				);
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		Optional<User> tempUser=repo.findByUsername(loginRequest.getUsername());
		User user=tempUser.orElseThrow();
		String token=jwtService.generateToken(user);
		return new AuthenticationResponse(token,refreshTokenService.generateRefreshToken().getToken(),Instant.now().plusMillis(900000),loginRequest.getUsername());
	}
	  public boolean isLoggedIn() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
	    }
	  @Transactional(readOnly = true)
	    public User getCurrentUser() {
	        User principal =  (User) SecurityContextHolder.
	                getContext().getAuthentication().getPrincipal();
	        return repo.findByUsername(principal.getUsername())
	                .orElseThrow(() -> new UsernameNotFoundException("User name not found - " + principal.getUsername()));
	    }
	  public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
	        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
	        Optional<User> tempUser=repo.findByUsername(refreshTokenRequest.getUsername());
			User user=tempUser.orElseThrow();
			String token=jwtService.generateToken(user);
	        return AuthenticationResponse.builder()
	                .authenticationToken(token)
	                .refreshToken(refreshTokenRequest.getRefreshToken())
	                .expiresAt(Instant.now().plusMillis(900000))
	                .username(refreshTokenRequest.getUsername())
	                .build();
	    }

}