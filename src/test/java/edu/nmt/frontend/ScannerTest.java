package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		assertEquals(s.getTokens(), tester);
		
		//testing add.c
		s = new Scanner("test/add.c");
		tester = Scanner.scanfromfile("test/add.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing subtract.c
		s = new Scanner("test/subtract.c");
		tester = Scanner.scanfromfile("test/subtract.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing multiply.c
		s = new Scanner("test/multiply.c");
		tester = Scanner.scanfromfile("test/multiply.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing divide.c
		s = new Scanner("test/divide.c");
		tester = Scanner.scanfromfile("test/divide.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing while.c
		s = new Scanner("test/while.c");
		tester = Scanner.scanfromfile("test/while.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing string.c
		s = new Scanner("test/string.c");
		tester = Scanner.scanfromfile("test/string.tokens");
		s.scan();
//		System.out.println(s.getTokens());
//		System.out.println(tester);
		assertEquals(s.getTokens(), tester);
		
		//testing break.c
		s = new Scanner("test/break.c");
		tester = Scanner.scanfromfile("test/break.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing function.c
		s = new Scanner("test/function.c");
		tester = Scanner.scanfromfile("test/function.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing assignment_arith.c
		s = new Scanner("test/assignment_arith.c");
		tester = Scanner.scanfromfile("test/assignment_arith.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing goto.c
		s = new Scanner("test/goto.c");
		tester = Scanner.scanfromfile("test/goto.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing conditions.c
		s = new Scanner("test/conditions.c");
		tester = Scanner.scanfromfile("test/conditions.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing types.c
//		s = new Scanner("test/types.c");
//		tester = Scanner.scanfromfile("test/types.tokens");
//		s.scan();
//		assertEquals(s.getTokens(), tester);
		
		//testing for.c
		s = new Scanner("test/for.c");
		tester = Scanner.scanfromfile("test/for.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing binary.c
		s = new Scanner("test/binary.c");
		tester = Scanner.scanfromfile("test/binary.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing assign_bin.c
		s = new Scanner("test/assignment_bin.c");
		tester = Scanner.scanfromfile("test/assignment_bin.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing switch.c
		s = new Scanner("test/switch.c");
		tester = Scanner.scanfromfile("test/switch.tokens");
		s.scan();
		assertEquals(s.getTokens(), tester);
		
		//testing struct.c
		s = new Scanner("test/struct.c");
		tester = Scanner.scanfromfile("test/struct.tokens");
		s.scan();
//		System.out.println(s.getTokens());
//		System.out.println(tester);
		assertEquals(s.getTokens(), tester);
		
		//testing pointer.c
		s = new Scanner("test/pointer.c");
		tester = Scanner.scanfromfile("test/pointer.tokens");
		s.scan();
//		System.out.println(s.getTokens());
//		System.out.println(tester);
		assertEquals(s.getTokens(), tester);

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
		baseline.add(new Token("int", 1, 1));
		baseline.add(new Token("main", 1, 5));
		baseline.add(new Token("(", 1, 9));
		baseline.add(new Token(")",1, 10));
		baseline.add(new Token("{", 2, 1));
		baseline.add(new Token("return", 3, 9));
		baseline.add(new Token("1", 3, 16));
		baseline.add(new Token(";", 3, 17));
		baseline.add(new Token("}", 4, 1));
		
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
		Scanner s = new Scanner("test/string.c");
		s.scan();
		s.offloadToFile();
		List<Token> baseline = Scanner.scanfromfile("test/string.tokens");
		List<Token> subject = Scanner.scanfromoffload("build/tokens.txt");
		assertEquals(subject, baseline);
	}

}
