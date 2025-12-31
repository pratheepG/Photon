package com.photon.identity.authentication.service;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;


	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws ApplicationException {
		Optional<User> user = userRepository.findByUserName(username);
		if (user.isPresent()) {
			if(!user.get().isEnabled()) {
				throw new ApplicationException(ExceptionEnum.ERR_1021.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
			}
			return user.get();
		}
		throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.UNAUTHORIZED);
	}

}