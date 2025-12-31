//package com.photon.identity.authentication.entrypoint;
//
//import java.io.IOException;
//import java.io.Serial;
//import java.io.Serializable;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.photon.constants.ResponseConstant;
//import com.photon.dto.ApiResponseDto;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.stereotype.Component;
//
//
///**
// * @author pratheepg
// *
// */
//@Component
//public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {
//
//	@Serial
//	private static final long serialVersionUID = 4882678978489056035L;
//
//	private static final Logger Logger = LogManager.getLogger(JwtAuthenticationEntryPoint.class);
//
//	@Override
//	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//
//		Logger.error("MESSAGE : "+authException.fillInStackTrace().toString());
//
//		response.setStatus(HttpStatus.UNAUTHORIZED.value());
//		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		ObjectMapper mapper = new ObjectMapper();
//		ApiResponseDto<?> errorResponse = new ApiResponseDto<>(-1,false, ResponseConstant.UN_AUTHORIZED);
//		mapper.writeValue(response.getWriter(), errorResponse);
//	}
//}