package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;
//import java.util.stream.Collectors.toList;
import org.hibernate.mapping.Collection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.SubredditDto;
import com.example.demo.exception.SpringRedditException;
import com.example.demo.mapper.SubredditMapper;
import com.example.demo.model.Subreddit;
import com.example.demo.repository.SubredditRepository;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class SubredditService {
	private final SubredditRepository subredditRepository;
	private final SubredditMapper subredditMapper;
	@Transactional
	public SubredditDto save(SubredditDto subredditDto) {
		Subreddit save=subredditRepository.save(subredditMapper.mapDtoToSubreddit(subredditDto));
		subredditDto.setId(save.getId());
		return subredditDto;
	}

	
	@Transactional(readOnly = true)
	public List<SubredditDto> getAll() {
		return subredditRepository.findAll()
		.stream()
		.map(subredditMapper::mapSubredditToDto)
		.collect(Collectors.toList());
		
	}


	public SubredditDto getSubreddit(Long id) {
		Subreddit subreddit=subredditRepository.findById(id)
				.orElseThrow(()->new SpringRedditException("No subreddit with given id : "+id));
		return subredditMapper.mapSubredditToDto(subreddit);
	}
	
	
}
