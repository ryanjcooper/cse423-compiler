package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

public class ScannerTest {

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

}
