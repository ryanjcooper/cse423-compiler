package edu.nmt.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.nmt.RuntimeSettings;

/**
 * TODO: Add documentation
 * @author Terence
 *
 */
public class TokenLabeler {		
	
	/**
	 * isNumeric function copied from baeldung
	 * ref: https://www.baeldung.com/java-check-string-number
	 * Section 3.
	 * @param str is string to check for values
	 * @return true if the string can be converted, else false
	 */
	public static boolean isNumeric(String str) {
	    if (str == null) {
	        return false;
	    }
	    
	    if (str.startsWith("0x")) {
	    	try {
	    		Integer.decode(str);
	    	} catch (NumberFormatException nfe) {
		        return false;
		    }
	    } else {
		    try {
		        int d = Integer.parseInt(str);
		    } catch (NumberFormatException nfe) {
		        return false;
		    }
	    }
	    
	    return true;
	}
	
	/**
	 * Checks if a token is an identifier
	 * @param str is string to check
	 * @return boolean true if it matches the ident. pattern, else false
	 */
	public static boolean isIdentifier(String str) {		
		Pattern pattern = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
		if (str != null) {
			Matcher matcher = pattern.matcher(str);
			return matcher.matches();
		} else {
			return false;
		}
	}
	
	/**
	 * Driver function to set up naming
	 * @param tokenString is token to auto-label
	 * @return String with auto-generated label
	 */
	public static String labelToken(String tokenString) {
		String label = RuntimeSettings.labeledTokenMap.get(tokenString);
		
		/* check if token is defined token, number, or id */
		if (label != null)
			return label;
		else if (isNumeric(tokenString))
			return "numeric_constant";
		else if (isIdentifier(tokenString))
			return "identifier";
		else
			return "unknown";
		
	}
	
	public static void main(String argv[]) {
//		System.out.println(isIdentifier("0m__1__ate"));
		System.out.println(isNumeric("0xabcdef"));
	}
}