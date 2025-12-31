package com.photon.identity.authentication.utils;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.photon.enums.ExceptionEnum;
import com.photon.exception.ApplicationException;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.idp.dto.CertificateDto;
import com.photon.identity.idp.dto.IdentityProviderDto;
import com.photon.identity.authentication.entity.RefreshToken;
import com.photon.properties.ApplicationConfigProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

@Slf4j
@Getter
public class JwtTokenUtil implements Serializable {

	@Serial
	private static final long serialVersionUID = -505738058103230295L;
	private final KeyStore keyStore;
	private final Key key;
	private final IdentityProviderDto identityProviderDto;
	private final CertificateDto certificateDetails;

	public JwtTokenUtil(IdentityProviderDto identityProviderDto) throws ApplicationException {
        try {
			this.identityProviderDto = identityProviderDto;
			this.certificateDetails = Objects.requireNonNull(this.identityProviderDto.getCertificate(), "Certificate details cannot be null");

			String keystoreType = Objects.requireNonNull(this.certificateDetails.getKeystoreType(), "Keystore type cannot be null");
			keyStore = KeyStore.getInstance(keystoreType);

			byte[] decodedJks = Objects.requireNonNull(this.certificateDetails.getKeystore(), "Keystore content cannot be null");
			try (InputStream jksInputStream = new ByteArrayInputStream(decodedJks)) {
				keyStore.load(jksInputStream, Objects.requireNonNull(this.certificateDetails.getKeyPassword(), "Keystore password cannot be null").toCharArray());
			}

			key = keyStore.getKey(Objects.requireNonNull(this.certificateDetails.getAlias(), "Key alias cannot be null"),
					Objects.requireNonNull(this.certificateDetails.getKeyPassword(), "Key password cannot be null").toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 IOException | CertificateException e) {
			throw new ApplicationException(ExceptionEnum.ERR_1004.getErrorResponseBody(e.getMessage()), HttpStatus.UNAUTHORIZED);
		}
	}

	public String generateToken(String sub, Map<String, Object> claims, String authType, AuthAdaptor adaptor, ApplicationConfigProperties applicationConfigProperties) {
		return doGenerateToken(claims, sub, authType, adaptor, applicationConfigProperties);
	}

	private String doGenerateToken(Map<String, Object> claims, String subject, String authType, AuthAdaptor adaptor, ApplicationConfigProperties applicationConfigProperties) {
		try {
			Map<String, Object> customHeaders = new HashMap<>();
			customHeaders.put("kid", String.valueOf(this.certificateDetails.getId()));
			customHeaders.put("jku", "https://my.identity.com/.well-known/jwks.json");
			customHeaders.put("typ", "JWT");
			customHeaders.put("cty", "JWT");
			customHeaders.put("idp", this.identityProviderDto.getId());
			customHeaders.put("auth-type", authType);
			customHeaders.put("auth-adaptor", adaptor.getName());
			customHeaders.put("x-api-key", applicationConfigProperties.getXApiKey());
			customHeaders.put("x-api-secret", applicationConfigProperties.getXApiSecret());
			if(this.identityProviderDto.getIncludeX509InJwt())
				this.addX509(customHeaders);
		return Jwts.builder()
				.setHeader(customHeaders)
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + (this.identityProviderDto.getSessionTimeoutMinutes() * 60L * 1000L)))
				.signWith(this.identityProviderDto.getSignatureAlgorithm(), key)
				.setId(UUID.randomUUID().toString())
				.compact();
		} catch (Exception e){
			log.error(e.getMessage());
		    return null;
		}
	}

	public RefreshToken createRefreshToken(Long refreshExpirationDateInMs) {
		RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationDateInMs * 60L * 1000L));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRefreshCount(0L);
        return refreshToken;
	}

	public boolean validateToken(String authToken) {
		try {
			Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
			log.info(" deviceId from JWT Token : {}", claims.getBody().get("deviceId"));
			return true;
		} catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
			throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
		}
    }

	public Date getTokenExpiryFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

	public String getUsernameFromToken(String token) throws Exception {
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
		return claims.getSubject();
	}

	private void addX509(Map<String, Object> headers) throws Exception {
		Certificate[] certChain = keyStore.getCertificateChain(this.certificateDetails.getAlias());

		if (certChain != null && certChain.length > 0) {
			X509Certificate leafCert = (X509Certificate) certChain[0];

			/* SHA-1 Thumbprint */
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			String x5t = Base64.getUrlEncoder().withoutPadding().encodeToString(sha1.digest(leafCert.getEncoded()));

			/* SHA-256 Thumbprint */
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			String x5tS256 = Base64.getUrlEncoder().withoutPadding().encodeToString(sha256.digest(leafCert.getEncoded()));

			/* x5c - Base64-encoded cert chain (all, not just leaf) */
			List<String> x5cList = Arrays.stream(certChain)
					.map(cert -> {
						try {
							return Base64.getEncoder().encodeToString(cert.getEncoded());
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					})
					.collect(Collectors.toList());

			headers.put("x5u", "https://my.identity.com/cert.pem");
			headers.put("x5c", x5cList);
			headers.put("x5t", x5t);
			headers.put("x5t#S256", x5tS256);
		}

	}

}