package com.photon.identity.authentication.annotations;

import com.photon.identity.commons.enums.LoginFactor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackLoginHistory {
    LoginFactor loginType();
}