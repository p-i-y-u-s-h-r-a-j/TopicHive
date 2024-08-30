package com.example.demo.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService{

	private final UserRepository userRepository;
	@Override
	@Transactional(readOnly=true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> userOptional=userRepository.findByUsername(username);
		return userOptional.orElseThrow(()->new UsernameNotFoundException("No User with given username found"));	
	}
	private Collection<? extends GrantedAuthority> getAuthorities(String role) {
		return Collections.singletonList(new SimpleGrantedAuthority(role));
	}


}
