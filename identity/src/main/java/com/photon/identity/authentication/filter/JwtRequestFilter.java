//package com.photon.identity.authentication.filter;
//
//import java.io.IOException;
//import java.util.Collections;
//
//import com.photon.constants.ResponseConstant;
//import com.photon.dto.ApiResponseDto;
//import com.photon.identity.authentication.service.JwtUserDetailsService;
//import com.photon.identity.idp.utils.IdentityProviderHandler;
//import com.photon.identity.authentication.utils.JwtTokenUtil;
//import com.photon.properties.ApplicationConfigProperties;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.AntPathMatcher;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import io.jsonwebtoken.ExpiredJwtException;
//
///**
// * @author pratheepg
// */
//@Component
//public class JwtRequestFilter extends OncePerRequestFilter {
//
//	private static final Logger Logger = LogManager.getLogger(JwtRequestFilter.class);
//
//	private final JwtUserDetailsService jwtUserDetailsService;
//	private final ApplicationConfigProperties applicationConfigProperties;
//	private final IdentityProviderHandler identityProviderHandler;
//
//    public JwtRequestFilter(JwtUserDetailsService jwtUserDetailsService, ApplicationConfigProperties applicationConfigProperties, IdentityProviderHandler identityProviderHandler) {
//        this.jwtUserDetailsService = jwtUserDetailsService;
//        this.applicationConfigProperties = applicationConfigProperties;
//        this.identityProviderHandler = identityProviderHandler;
//    }
//
//    @Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//			throws ServletException, IOException {
//		try {
//
//			final String requestTokenHeader = request.getHeader("Authorization");
//			final String requestAPIKeyHeader = request.getHeader("X-API-KEY");
//			final String requestAPISecretHeader = request.getHeader("X-API-SECRET");
//			final String requestReportingParamsHeader = request.getHeader("ReportingParams");
//			final String provider = request.getHeader("Provider");
//
//			String username = null;
//			String jwtToken = null;
//
//			JwtTokenUtil jwtTokenUtil = new JwtTokenUtil(this.identityProviderHandler.getIdentityProvider(provider));
//
//			// JWT Token is in the form "Bearer token". Remove Bearer word and get
//			// only the Token
//			Logger.warn("JWT token not found : "+request.getServletPath());
//			if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
//				jwtToken = requestTokenHeader.substring(7);
//
//				username = jwtTokenUtil.getUsernameFromToken(jwtToken);
//
//			}
//			else if(requestAPIKeyHeader != null && requestAPISecretHeader != null) {
//                Logger.warn("requestAPIKeyHeader : {}", requestAPIKeyHeader);
//                Logger.warn("requestAPISecreHeader : {}", requestAPISecretHeader);
//                Logger.warn("APIKey : {}", this.applicationConfigProperties.getCompositeXApiKey());
//
//				if(requestAPIKeyHeader.equals(this.applicationConfigProperties.getCompositeXApiKey()) && requestAPISecretHeader.equals(this.applicationConfigProperties.getCompositeXApiSecret())) {
//                    Logger.warn("ReportingParams : {}", requestReportingParamsHeader);
//					request.getSession().setAttribute("ReportingParams", requestReportingParamsHeader);
//
//					AnonymousAuthenticationToken anonymousAuth = new AnonymousAuthenticationToken(
//							"AnonymousUser", "AnonymousUser",
//							Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
//					SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
//
//				} else {
//                    Logger.error("Invalid client credentials : {}", requestTokenHeader);
//					response.setStatus(HttpStatus.UNAUTHORIZED.value());
//					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//					ObjectMapper mapper = new ObjectMapper();
//					ApiResponseDto<?> errorResponse = new ApiResponseDto<>(-1,false, ResponseConstant.TOKEN_NOT_FOUND);
//					mapper.writeValue(response.getWriter(), errorResponse);
//					return;
//				}
//			}
//			else if(requestTokenHeader == null){
//                Logger.error("JWT token not found : {}", requestTokenHeader);
//				response.setStatus(HttpStatus.UNAUTHORIZED.value());
//				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//				ObjectMapper mapper = new ObjectMapper();
//				ApiResponseDto<?> errorResponse = new ApiResponseDto<>(-1,false, ResponseConstant.TOKEN_NOT_FOUND);
//				mapper.writeValue(response.getWriter(), errorResponse);
//				return;
//			} else if(!requestTokenHeader.startsWith("Bearer ")){
//                Logger.error("JWT token not starts with Bearer : {}", requestTokenHeader);
//				response.setStatus(HttpStatus.UNAUTHORIZED.value());
//				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//				ObjectMapper mapper = new ObjectMapper();
//				ApiResponseDto<?> errorResponse = new ApiResponseDto<>(-1,false, ResponseConstant.INVALID_TOKEN_FORMAT);
//				mapper.writeValue(response.getWriter(), errorResponse);
//				return;
//			} else {
//                Logger.error("Error on JWT token : {}", requestTokenHeader);
//				response.setStatus(HttpStatus.UNAUTHORIZED.value());
//				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//				ObjectMapper mapper = new ObjectMapper();
//				ApiResponseDto<?> errorResponse = new ApiResponseDto<>(-1,false, ResponseConstant.UN_AUTHORIZED);
//				mapper.writeValue(response.getWriter(), errorResponse);
//				return;
//			}
//
//			// Once we get the token validate it.
//			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//
//				UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
//
//				// if token is valid configure Spring Security to manually set
//				// authentication
//				if (jwtTokenUtil.validateToken(jwtToken)) {
//
//					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//					usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//
//
//					// After setting the Authentication in the context, we specify
//					// that the current user is authenticated. So it passes the
//					// Spring Security Configurations successfully.
//					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//				}
//			}
//			chain.doFilter(request, response);
//		} catch (ExpiredJwtException e) {
//			Logger.error("Expired JWT Token ");
//			response.setStatus(HttpStatus.UNAUTHORIZED.value());
//			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//			ObjectMapper mapper = new ObjectMapper();
//			ApiResponseDto<?> errorResponse = new ApiResponseDto<>(-1,false, ResponseConstant.EXPIRED_JWT_TOKEN);
//			mapper.writeValue(response.getWriter(), errorResponse);
//        } catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//
//	@Override
//	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//
//		AntPathMatcher antPathMatcher = new AntPathMatcher();
//		boolean isOpenApi = true;
//		String method = request.getMethod();
//		String requestUri = request.getRequestURI().concat((StringUtils.hasText(request.getQueryString()))? "?".concat(request.getQueryString()):"");
//		String requestParam = request.getQueryString();
//
//        Logger.info("Request Method : {}", method);
//        Logger.info("Request Param : {}", requestParam);
//        Logger.info("Request URI : {}", requestUri);
//
//		return isOpenApi;
//	}
//}
