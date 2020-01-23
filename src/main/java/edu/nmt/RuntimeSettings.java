package edu.nmt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;

public class RuntimeSettings {

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
	
	static {
		File tokenCfg = new File("config/tokens_and_labels.cfg");
		
		if (tokenCfg.exists()) {
			/* attempt to read in config file */
			try {
				Scanner sc = new Scanner(tokenCfg);
				
				/* go through the config file 
				 * each line begins with a label followed by all
				 * corresponding symbols
				 */
				while (sc.hasNextLine()) {
					String[] tokenList = sc.nextLine().split(" ");
					String label = tokenList[0]; // this is the label for this set of tokens
					
					for (int i = 1; i < tokenList.length; i++) {
						labeledTokenMap.put(tokenList[i], label);
					}
				}
				
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
	
	

}
