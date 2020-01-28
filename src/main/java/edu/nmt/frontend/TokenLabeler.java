package edu.nmt.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import edu.nmt.RuntimeSettings;

/**
 * TODO: Add documentation
 * @author Terence
 *
 */
public class TokenLabeler {		
	
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
	
	public static TokenLabel labelToken(String tokenString) {
		String label = RuntimeSettings.labeledTokenMap.get(tokenString);
		
		/* check if token is defined token, number, or id */
		if (label != null)
			return TokenLabel.valueOf(label);
		else if (isNumeric(label))
			return TokenLabel.valueOf("numConstant");
		else
			return TokenLabel.valueOf("identifier");
		
	}
	
	public static void main(String[] argv) {
		System.out.println(labelToken("main"));
		System.out.println(labelToken("foobar"));
		System.out.println(labelToken("\""));
	}
}
