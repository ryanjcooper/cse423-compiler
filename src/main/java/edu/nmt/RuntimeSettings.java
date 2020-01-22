package edu.nmt;

import java.io.File;
import java.io.IOException;

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
	
	

}
