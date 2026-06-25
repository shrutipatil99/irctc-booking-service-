package com.irctc.booking_service.Util;

//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.mindrot.jbcrypt.BCrypt;
public class UserServiceUtil {

	/*
	    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	    public static String hashPassword(String plainPassword) {
	        return encoder.encode(plainPassword);
	    }

	    public static boolean checkPassword(String plainPassword, String hashedPassword) {
	        return encoder.matches(plainPassword, hashedPassword);
	    }
	    */
	
	 public static String hashPassword(String plainPassword) {
	        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
	    }

	    public static boolean checkPassword(String plainPassword, String hashedPassword) {
	        return BCrypt.checkpw(plainPassword, hashedPassword);
	    }
}
