package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

public class GrammarTest {
	String invalidInput = "thisfiledoesnotexist";
	String validInput = "config/grammar.cfg";

	@Test(expected = FileNotFoundException.class)
	public void testGrammarFile_FileDoesNotExist_ExceptionThrown() throws IOException {
		File inp = new File(invalidInput);
		try {
			Grammar g = new Grammar(inp);
			fail("Exception should have been thrown");
		} catch (FileNotFoundException e) {
			throw e;
		}
	}

	@Test
	public void testGrammarFile_FileExists_NoException() throws IOException {
		File inp = new File(validInput);
		Grammar g = new Grammar(inp);
	}
	
	@Test(expected = FileNotFoundException.class)
	public void testGrammarString_InvalidString_ExceptionThrown() throws IOException {
		try {
			Grammar g = new Grammar(invalidInput);
			fail("Exception should have been thrown");
		} catch (FileNotFoundException e) {
			throw e;
		}
	}
	
	@Test
	public void testGrammarString_ValidString_NoException() throws IOException {
		Grammar g = new Grammar(validInput);
	}
	
	@Test
	public void testLoadGrammar() throws IOException {
		Grammar g = new Grammar(validInput);
		g.loadGrammar();
//		BufferedReader reader = new BufferedReader(new FileReader(validInput));
//		String line = null;
		
//		reader.mark(0);
//		do {
//			line = reader.readLine();
//			
//			if (!line.contains("\t") && !(line.trim().isEmpty())) {
//				
//			}
//		} while (line != null);
//		reader.reset();
		
		assertNotNull(g.getRules());
		assertNotNull(g.getRules("program"));
		assertNotNull(g.getVariables());
		assertNotNull(g.getFirstSets());
		assertNotNull(g.getFollowSets());
		
//		reader.close();
	}
}
