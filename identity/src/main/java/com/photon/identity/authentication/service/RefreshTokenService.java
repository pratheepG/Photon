/**
 * 
 */
package com.photon.identity.authentication.service;

import java.time.Instant;
import java.util.Optional;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.authentication.entity.RefreshToken;
import com.photon.identity.authentication.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author pratheepg
 *
 */
@Service
public class RefreshTokenService {
	
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;
	
	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}
	
	public RefreshToken save(RefreshToken refreshToken) {
		return refreshTokenRepository.save(refreshToken);
	}
	
	public void delete(RefreshToken token) {
		refreshTokenRepository.delete(token);
	}
	
	public RefreshToken increaseCount(RefreshToken refreshToken) {
		refreshToken.incrementRefreshCount();
		return save(refreshToken);
	}
	
	public void checkExpiriation(RefreshToken refreshToken) {
		if(refreshToken.getExpiryDate().compareTo(Instant.now())<0){
			throw new ApplicationException(ExceptionEnum.ERR_1002.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
		}
	}

}
