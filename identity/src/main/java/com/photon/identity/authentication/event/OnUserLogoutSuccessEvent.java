package com.photon.identity.authentication.event;

import java.io.Serial;
import java.time.Instant;
import java.util.Date;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OnUserLogoutSuccessEvent extends ApplicationEvent {

	@Serial
	private static final long serialVersionUID = 1L;
	private final String userEmail;
    private final String token;
    private final String idp;
    private final Date eventTime;
    
    public OnUserLogoutSuccessEvent(String userEmail, String token, String idp) {
        super(userEmail);
        this.userEmail = userEmail;
        this.token = token;
        this.idp = idp;
        this.eventTime = Date.from(Instant.now());
    }

}
