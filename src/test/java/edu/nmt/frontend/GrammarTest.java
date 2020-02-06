package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import edu.nmt.util.IOUtil;

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
		
		int i = 0;
		String fcontents = IOUtil.readFileToString(new File(validInput));
		String[] lines = fcontents.split("\n");
		
		//testing that rules are set correctly based on grammar file using getRules() method
		String LHS = null;
		for (String line : lines) {
			if (!line.contains("\t") && (!line.trim().isEmpty())) {
				LHS = line.trim();
			} else if (line.trim().isEmpty()) {
				continue;
			} else {
				String[] RHS = line.trim().split("\\s+");
				//once LHS, RHS pair has been formed, perform the following checks:
				//assert LHS encountered in file matches the i-th LHS
				assertEquals(g.getRules().get(i).getLeftSide(), LHS);
				//assert RHS and i-th RHS have matching array lengths
				boolean len = (g.getRules().get(i).getRightSide().length == RHS.length);
				assertTrue(len);
				
				if (len) {
					//assert each String in the RHS pairs are equivalent
					for (int j = 0; j < g.getRules().get(i).getRightSide().length; j++) {
						assertTrue(g.getRules().get(i).getRightSide()[j].contentEquals(RHS[j]));
					}
				}
				
				i++;
			}
		}
		
		
		assertNotNull(g.getRules("program"));
		assertNotNull(g.getVariables());
		assertNotNull(g.getFirstSets());
		assertNotNull(g.getFollowSets());
	}
}
