package com.photon.identity.authentication.cache;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.photon.identity.authentication.event.OnUserLogoutSuccessEvent;
import com.photon.identity.idp.utils.AuthTypeProvider;
import com.photon.identity.idp.utils.IdentityProviderHandler;
import com.photon.identity.authentication.utils.JwtTokenUtil;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggedOutJwtTokenCache {
	private static final Logger logger = LoggerFactory.getLogger(LoggedOutJwtTokenCache.class);

    private final ExpiringMap<String, OnUserLogoutSuccessEvent> tokenEventMap;
    private final IdentityProviderHandler identityProviderHandler;

    @Autowired
    public LoggedOutJwtTokenCache(AuthTypeProvider authTypeProvider, IdentityProviderHandler identityProviderHandler) {
        this.identityProviderHandler = identityProviderHandler;
        this.tokenEventMap = ExpiringMap.builder()
                .variableExpiration()
                .maxSize(1000)
                .build();
    }

    public void markLogoutEventForToken(OnUserLogoutSuccessEvent event) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(this.identityProviderHandler.getIdentityProvider(event.getIdp()));
        String token = event.getToken();
        if (tokenEventMap.containsKey(token)) {
            logger.info(String.format("Log out token for user [%s] is already present in the cache", event.getUserEmail()));

        } else {
            Date tokenExpiryDate = jwtTokenUtil.getTokenExpiryFromJWT(token);
            long ttlForToken = getTTLForToken(tokenExpiryDate);
            logger.info(String.format("Logout token cache set for [%s] with a TTL of [%s] seconds. Token is due expiry at [%s]", event.getUserEmail(), ttlForToken, tokenExpiryDate));
            tokenEventMap.put(token, event, ttlForToken, TimeUnit.SECONDS);
        }
    }

    public OnUserLogoutSuccessEvent getLogoutEventForToken(String token) {
        return tokenEventMap.get(token);
    }

    private long getTTLForToken(Date date) {
        long secondAtExpiry = date.toInstant().getEpochSecond();
        long secondAtLogout = Instant.now().getEpochSecond();
        return Math.max(0, secondAtExpiry - secondAtLogout);
    }
}