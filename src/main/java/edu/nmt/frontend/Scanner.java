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

/**
 * Scanner and tokenizer
 * @dated 01/22/20
 * @author Ryan
 *
 */
public class Scanner {
	
	// Class Variables
	private File finp; // input file object
	private static final String tokenOffloadFile = RuntimeSettings.buildDir + "/" + "tokens.txt"; // file to write tokens to
	private static String punctuation = "'!\"#$%&\\'()*+,-./:;<=>?@[\\\\]^_`{|}~"; // punctuation
	List<Token> tokens; // list of tokens
	
	
	private static final String[][] doublePunctCases = {{"\\+\\s\\+", "\\+\\+"}, // ++
														{"\\-\\s\\-", "\\-\\-"}, // --
														{"\\&\\s\\&", "\\&\\&"}, // &&
														{"\\|\\s\\|", "\\|\\|"}, // ||
														{"\\+\\s\\=", "\\+\\="}, // +=
														{"\\-\\s\\=", "\\-\\="}, // -=
														{"\\*\\s\\=", "\\*\\="}, // *=
														{"\\/\\s\\=", "\\/\\="}, // /=
														{"\\&\\s\\=", "\\&\\="}, // &=
														{"\\%\\s\\=", "\\%\\="}, // %=
														{"\\|\\s\\=", "\\|\\="}, // |=
														{"\\^\\s\\=", "\\^\\="}, // ^=
														{"\\~\\s\\=", "\\~\\="}, // ~=
														{"\\=\\s\\=", "\\=\\="}, // ==
														{"\\<\\s\\=", "\\<\\="}, // <=
														{"\\>\\s\\=", "\\>\\="}, // >=
													   };
														
	
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
		
		/* Preprocess input prior to tokenization */
		
		// Remove single line comments
		fcontents = fcontents.replaceAll("//.*\n", " ");
		
		// Remove multiline comments (non-greedy search, otherwise we might delete code)
		fcontents = fcontents.replaceAll("(?s)/\\*.*?\\*/", " ");
		
		// Add whitespace around punctuation characters
		for (char c : punctuation.toCharArray()) {
			String cs = Character.toString(c);
			fcontents = fcontents.replaceAll("\\" + cs, " " + "\\" + cs + " ");
		}
		
		// Remove repeated whitespace
		fcontents = fcontents.replaceAll("\\s+", " ");

		// Remove whitespace for special punctuation tokens (*=, +=, &&, etc)
		for (int i = 0; i < doublePunctCases.length; i++) {
			fcontents = fcontents.replaceAll(doublePunctCases[i][0], doublePunctCases[i][1]);
		}
		
		// Send processed code to tokenizer
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
	    	System.out.println(tok.toString()); // TODO: Remove in future
	    	writer.write(tok.toString() + '\n');
	    }     
	    writer.close();
	}
	
	
    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner("test/fizzbuzz.c");
        s.scan();
    }
	
	
}
