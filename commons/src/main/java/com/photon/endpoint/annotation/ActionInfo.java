package com.photon.endpoint.annotation;

import com.photon.endpoint.enums.AccessLevel;
import com.photon.enums.SecurityLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionInfo {
    String id();
    String name();
    String description() default "";
    SecurityLevel securityLevel() default SecurityLevel.PRIVATE;
    AccessLevel accessLevel() default AccessLevel.VIEWER;
}