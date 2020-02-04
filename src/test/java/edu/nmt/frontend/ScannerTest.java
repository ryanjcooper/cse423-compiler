package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ScannerTest {

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
		
		//testing bubblesort.c
		
		//testing bubblesort2.c
		
		//testing bubblesortline.c
		
		//testing exhaustive.c
		Scanner se = new Scanner("test/minimal.c");
		List<Token> testere = Scanner.scanfromfile("test/minimal.tokens");
		se.scan();
		assertEquals(se.getTokens(), testere);
		
		//testing fizzbuzz.c
		
		//testing hello_world.c
		
		//testing minimal.c
		Scanner sm = new Scanner("test/minimal.c");
		List<Token> testerm = Scanner.scanfromfile("test/minimal.tokens");
		sm.scan();
		assertEquals(sm.getTokens(), testerm);
		
		//Testing ops.c
		Scanner so = new Scanner("test/minimal.c");
		List<Token> testero = Scanner.scanfromfile("test/minimal.tokens");
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

}
