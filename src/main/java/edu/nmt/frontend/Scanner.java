package edu.nmt.frontend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import edu.nmt.RuntimeSettings;
import edu.nmt.util.IOUtil;

public class Scanner {
	
	private File finp;
	private static final String tokenOffloadFile = RuntimeSettings.buildDir + "/" + "tokens.txt";
	private static String punctuation = "'!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~";
	List<Token> tokens;
	
	public Scanner(File file) throws FileNotFoundException { 
		finp = file;
		if (!finp.exists()) {
			throw new FileNotFoundException();
		}
	}
	
	public Scanner(String fileName) throws FileNotFoundException { 
		finp = new File(fileName);
		if (!finp.exists()) {
			throw new FileNotFoundException();
		}
	}
	
	public void scan() throws IOException {
		String fcontents = IOUtil.readFileToString(finp);
		
		// Preprocess input prior to tokenization
		for (char c : punctuation.toCharArray()) {
			String cs = Character.toString(c);
			fcontents = fcontents.replaceAll("\\" + cs, " " + "\\" + cs);
		}
		
		fcontents = fcontents.replaceAll("\\s+", " ");
		
		tokens = tokenize(fcontents);
	
		offloadToFile();
	}
	
	private static List<Token> tokenize(String s) {
		ArrayList<Token> tokens = new ArrayList<Token>();
		StringTokenizer st = new StringTokenizer(s);
        
		while (st.hasMoreTokens()) 
        	tokens.add(new Token(st.nextToken()));
        
        return tokens;
	}
	
	private void offloadToFile() throws IOException {
	    BufferedWriter writer = new BufferedWriter(new FileWriter(tokenOffloadFile));
	    for (Token tok : tokens) {
	    	writer.write(tok.toString() + '\n');
	    }     
	    writer.close();
	}
	
	
    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner("test/minimal.c");
        s.scan();
    }
	
	
}
