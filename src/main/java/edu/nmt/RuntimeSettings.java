package edu.nmt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class RuntimeSettings {

	public static String sourceFilename;
	public static String outputFilename;
	
	public static final String buildDir;
	
	static {
		buildDir = "build";
		
        try {
        	File dir = new File(buildDir);
        	if (dir.exists() && dir.isDirectory()) {
                FileUtils.cleanDirectory(dir);
                FileUtils.forceDelete(dir);        		
        	}
            
            FileUtils.forceMkdir(dir);
        } catch (IOException e) {
            e.printStackTrace();
			System.err.println("Unable to create build directory, aborting...");
			System.exit(1);
        } 

	}
	
	public static HashMap<String,String> labeledTokenMap = new HashMap<String, String>();
	public static HashMap<String,String> token2LabelMap = new HashMap<String, String>();
	public static List<String> validLabels = new ArrayList<String>();
	
	static {
		File tokenCfg = new File("config/tokens_and_labels.cfg");
		
		if (tokenCfg.exists()) {
			/* attempt to read in config file */
			try {
				/* this is java.util Scanner, not the lexer Scanner */
				Scanner sc = new Scanner(tokenCfg);
				
				/* go through the config file 
				 * each line begins with a label followed by all
				 * corresponding symbols
				 */
				while (sc.hasNextLine()) {
					String[] tokenList = sc.nextLine().split(" ");
					String label = tokenList[0]; 				// this is the label for this set of tokens
					validLabels.add(label);
					token2LabelMap.put(label, tokenList[1]);	// this is strictly for 1 to 1 mappings
					for (int i = 1; i < tokenList.length; i++) {
						labeledTokenMap.put(tokenList[i], label);
					}
				}
				
				sc.close();
				
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Unable to load tokens and labels from " + tokenCfg.toString());
				System.exit(1);
			}
		} else {
			System.err.println("Unable to find " + tokenCfg.toString());
			System.exit(1);
		}
		
		
	}
	
	public final static String grammarFile = "config/grammar.cfg";
	
//	public static HashMap<String,String> grammarMap = new HashMap<String, String>();
//	
//	static {
//		File grammarCfg = new File("config/grammar.cfg");
//		
//		if (grammarCfg.exists()) {
//			/* attempt to read in grammar file */
//			try {
//				/* this is java.util Scanner, not the lexer Scanner */
//				Scanner sc = new Scanner(grammarCfg);
//				String nonTerminal = null; 						// think of this as a parent node				
//				
//				/* go through the grammar file 
//				 * each line begins with a label followed by all
//				 * corresponding symbols
//				 */
//				while (sc.hasNextLine()) {
//					String terminal = sc.nextLine();			// think of this as a child node
//					
//					/* if the line contains a tab, it is a terminal or child node
//					 * else it is a parent node
//					*/
//					if (!terminal.contains("\t")) {
//						nonTerminal = terminal;
//					} else if (nonTerminal == null) {
//						throw new Exception("Error in config"); // check if first line is parent
//					} else {
//						grammarMap.put(terminal.trim(), nonTerminal.trim());
//					}
//				}
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.err.println("Unable to load grammar from " + grammarCfg.toString());
//				System.exit(1);
//			}
//		} else {
//			System.err.println("Unable to find " + grammarCfg.toString());
//			System.exit(1);
//		}
//	}	

}
