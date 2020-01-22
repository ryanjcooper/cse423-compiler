package edu.nmt.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TokenLabler {
	public enum TokenLabel {
	    variableModifier,
	    typeSpecifier,
	    controlSpecifier,
	    identifier,
	    numConstant,
	    tagSpecifier,
	    assignmentSpecifier,
	    integerOperator,
	    booleanOperator,
	    conditionalSpecifier,
	    iterationStmt,
	    structSpecifier,
	    switchStmt,
	    enumSpecifier,
	    unionSpecifier,
	    typedefSpecifier,
	    sizeofSpecifier,
	    parameterDelimiter,
	    delimiters,
	    bitOperator
	}
	
	public static HashMap<String,String> tokenMap = new HashMap<String, String>();
	
	public static void readConfig(String filename) {
		/* attempt to read in config file */
		try {
			Scanner sc = new Scanner(new File(filename));
			
			/* go through the config file 
			 * each line begins with a label followed by all
			 * corresponding symbols
			 */
			while (sc.hasNextLine()) {
				String[] tokenList = sc.nextLine().split(" ");
				String label = tokenList[0]; // this is the label for this set of tokens
				
				for (int i = 1; i < tokenList.length; i++) {
					tokenMap.put(tokenList[i], label);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
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
	
	public static String labelToken(String tokenString) {
		String label = tokenMap.get(tokenString);
		
		/* check if token is defined token, number, or id */
		if (label != null)
			return label;
		else if (isNumeric(label))
			return "numConstant";
		else
			return "identifier";
		
	}
	
	public static void main(String[] argv) {
		readConfig("tokens_and_labels");
		System.out.println(labelToken("main"));
	}
}
