package com.photon.identity.authentication.event.listener;

import com.photon.identity.authentication.cache.LoggedOutJwtTokenCache;
import com.photon.identity.authentication.event.OnUserLogoutSuccessEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OnUserLogoutSuccessEventListener implements ApplicationListener<OnUserLogoutSuccessEvent> {

    private final LoggedOutJwtTokenCache tokenCache;
    private static final Logger logger = LoggerFactory.getLogger(OnUserLogoutSuccessEventListener.class);

    @Autowired
    public OnUserLogoutSuccessEventListener(LoggedOutJwtTokenCache tokenCache) {
        this.tokenCache = tokenCache;
    }

    public void onApplicationEvent(OnUserLogoutSuccessEvent event) {
        logger.info(String.format("Log out success event received for user [%s]", event.getUserEmail()));
        tokenCache.markLogoutEventForToken(event);
    }
}
