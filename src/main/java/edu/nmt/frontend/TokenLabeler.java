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
	 */
	public static boolean isNumeric(String str) {
	    if (str == null) {
	        return false;
	    }
	    
	    try {
	        int d = Integer.parseInt(str);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    
	    return true;
	}
	
	public static boolean isIdentifier(String str) {		
		Pattern pattern = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");
		if (str != null) {
			Matcher matcher = pattern.matcher(str);
			return matcher.matches();
		} else {
			return false;
		}
	}
	
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
		System.out.println(isIdentifier("0m__1__ate"));
	}
}