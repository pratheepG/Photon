/**
 * 
 */
package com.photon.identity.authentication.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;


/**
 * @author pratheepg
 *
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, FIELD, PARAMETER})
@AuthenticationPrincipal
public @interface CurrentUser {

}
