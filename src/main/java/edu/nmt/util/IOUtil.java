package edu.nmt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class IOUtil {
	
	public static String readFileToString(File file) throws IOException {
		StringBuilder sb = new StringBuilder(); 
		String st;
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		while ((st = br.readLine()) != null) {
			sb.append(st);
		}
		return sb.toString();
	}
	
}
