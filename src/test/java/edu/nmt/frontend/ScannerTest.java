package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class ScannerTest {
	
	/**
	 * Test the constructor that takes in type File
	 */
	@Test
	public void testScannerFile() {
		
		try {
			File breakit = new File("notavirus.exe");
			Scanner s = new Scanner(breakit); // Should fail
		} catch (FileNotFoundException e) {
			assertTrue(true);
		}
		
		// Should not throw an error
		
		try {
			File dontbreak = new File("test/base.tokens");
			Scanner s = new Scanner(dontbreak);
		} catch (FileNotFoundException e) {
			assertTrue(false);
		}
	}
	
	/**
	 * Test the constructor that takes in a string
	 */
	@Test
	public void testFileString() {
		try {
			// Should pass assuming the make has been run
			Scanner s = new Scanner("test/base.tokens");
		} catch (FileNotFoundException e) {
			assertTrue(false);
		}
		
		try {
			Scanner t = new Scanner("404filenotfound.txt");
		} catch (FileNotFoundException e) {
			// This is expected behavior
			assertTrue(true);
		}
		
	}

	/**
	 * Tests if the Scanner output matches the clang Scanner
	 * @throws IOException
	 */
	@Test
	public void testScan() throws IOException {
		
		//testing base.c
		Scanner s = new Scanner("test/base.c");
		List<Token> tester = Scanner.scanfromfile("test/base.tokens");
		s.scan();
		System.out.println(tester);
		assertEquals(s.getTokens(), tester);
		
		//testing bubblesort.c
		
		//testing bubblesort2.c
		
		//testing bubblesortline.c
		
		//testing exhaustive.c
//		Scanner se = new Scanner("test/exhaustive.c");
//		List<Token> testere = Scanner.scanfromfile("test/exhaustive.tokens");
//		se.scan();
//		assertEquals(se.getTokens(), testere);
		
		//testing fizzbuzz.c
		
		//testing hello_world.c
		
		//testing minimal.c
		Scanner sm = new Scanner("test/minimal.c");
		List<Token> testerm = Scanner.scanfromfile("test/minimal.tokens");
		sm.scan();
		assertEquals(sm.getTokens(), testerm);
		
		//Testing ops.c
		Scanner so = new Scanner("test/ops.c");
		List<Token> testero = Scanner.scanfromfile("test/ops.tokens");
		so.scan();
		assertEquals(so.getTokens(), testero);
	}
	
	/**
	 * Tests if the scanfromfile function is correct
	 * @throws IOException 
	 */
	@Test
	public void testComp() throws IOException {
		List<Token> subject = Scanner.scanfromfile("test/base.tokens");
		List<Token> baseline = new ArrayList<Token>();
		
		// Assemble baseline
		baseline.add(new Token("int"));
		baseline.add(new Token("main"));
		baseline.add(new Token("("));
		baseline.add(new Token(")"));
		baseline.add(new Token("{"));
		baseline.add(new Token("return"));
		baseline.add(new Token("1"));
		baseline.add(new Token(";"));
		baseline.add(new Token("}"));
		
		assertEquals(subject, baseline);
	}
	
	/**
	 * Tests if a file is being offloaded correctly
	 * Compares two files after read in, as clang has different naming precedences
	 * @throws IOException
	 */
	@Test
	public void testOffload() throws IOException {
		// Scan a file to populate the test case
		Scanner s = new Scanner("test/base.c");
		s.scan();
		List<Token> baseline = Scanner.scanfromfile("test/base.tokens");
		List<Token> subject = Scanner.scanfromfile("build/tokens.txt");
		
		assertEquals(subject, baseline);
	}

}
