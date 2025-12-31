/**
 * 
 */
package com.photon.constants;

/**
 * @author pratheepg
 *
 */
public class RequestConstant {
	
	public static String AUTHORIZATION_HEADER = "Authorization";
	public static String BEARER = "Bearer ";
	public static String LOGIN_URL = "/authentication/login";
	public static String LOGOUT_URL = "/authentication/cusLogout";
	public static String REGISTER_URL = "/authentication/register";
	public static String DEVICE_REGISTER_URL = "/userdevice";
	public static String REFRESH_TOKEN_URL = "/authentication/refreshClaim";
	public static String IS_USER_INFO_EXIST_URL = "/authentication/isExist?phone=*&email=*";
	public static String IS_USER_INFO_EXIST_REGX = "/authentication/isExist\\?phone=.+&email=.+";
	
	
	public static String POST_GET_BYPAGE_URL = "/post?pageNumber=*&pageSize=*";
	public static String POST_GET_BYPAGE_REGX= "/post\\?pageNumber=.+&pageSize=.+";
	
	
	public static String REAL_ESTATE_URL = "/realestate";
	public static String REAL_ESTATE_OWNER_DETAILS_URL = "/realestate/owner?id=*";
	public static String REAL_ESTATE_OWNER_DETAILS_REGX = "/realestate/owner\\?id=.+";
	public static String REAL_ESTATE_BY_USER_URL = "/realestate?ids=*&limit=*&page=*";
	public static String REAL_ESTATE_BY_USER_REGX = "/realestate\\?ids=.+&limit=.+&page=.+";
	public static String REAL_ESTATE_FILE_UPLOAD_URL = "/realestate/upload/image";
	public static String REAL_ESTATE_SEARCH_ALL_URL = "/realestate/search/**";
	public static String REAL_ESTATE_SEARCH_URL ="/realestatee/search?propertyFor=*&propertyType=*&propertySubType=*&limit=*&page=*";
	public static String REAL_ESTATE_SEARCH_REGX ="/realestate/search\\?propertyFor=.+&propertyType=.+&propertySubType=.+&limit=.+&page=.+";
	
	
	public static String LOCALITY_URL = "/locality/**";
	public static String CLIENT_APP_CONFIG= "/appconfig";
	public static String SWAGGER= "/swagger-ui/**";
	public static String SWAGGER_V3= "/v3/**";
	public static String FILES= "/files/**";
	public static String OTP_URL= "/otp?contact=*";
	public static String OTP_REGX= "/otp\\?contact=.+";
	public static String VERIFY_OTP_URL= "/otp?Contactinfo=*&otp=*";
	public static String VERIFY_OTP_REGX= "/otp\\?Contactinfo=.+&otp=.+";
	public static String USER= "/user";

}
