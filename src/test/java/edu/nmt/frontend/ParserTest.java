package edu.nmt.frontend;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ParserTest {

	private Grammar grammar;
	
	@Before
	public void setup() throws IOException {
		grammar = new Grammar("config/grammar.cfg");
		grammar.loadGrammar();
	}
	
	@Test
	public void addTest() throws IOException {
		Scanner s = new Scanner("test/add.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void assign_binTest() throws IOException {
		Scanner s = new Scanner("test/assignment_bin.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void assign_arithTest() throws IOException {
		Scanner s = new Scanner("test/assignment_arith.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void baseTest() throws IOException {
		Scanner s = new Scanner("test/base.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void binaryTest() throws IOException {
		Scanner s = new Scanner("test/binary.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void booleanTest() throws IOException {
		Scanner s = new Scanner("test/boolean.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void breakTest() throws IOException {
		Scanner s = new Scanner("test/break.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void conditionsTest() throws IOException {
		Scanner s = new Scanner("test/conditions.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void divideTest() throws IOException {
		Scanner s = new Scanner("test/divide.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
//	@Test
//	public void enumTest() throws IOException {
//		Scanner s = new Scanner("test/enum.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}
	
//	@Test
//	public void exhaustiveTest() throws IOException {
//		Scanner s = new Scanner("test/exhaustive.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}
	
	@Test
	public void forTest() throws IOException {
		Scanner s = new Scanner("test/for.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void functionTest() throws IOException {
		Scanner s = new Scanner("test/function.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}

	@Test
	public void gotoTest() throws IOException {
		Scanner s = new Scanner("test/goto.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void minTest() throws IOException {
		Scanner s = new Scanner("test/min.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void multiplyTest() throws IOException {
		Scanner s = new Scanner("test/multiply.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
//	@Test
//	public void pointerTest() throws IOException {
//		Scanner s = new Scanner("test/pointer.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}

//	@Test
//	public void stringTest() throws IOException {
//		Scanner s = new Scanner("test/string.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}
	
//	@Test
//	public void structTest() throws IOException {
//		Scanner s = new Scanner("test/struct.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}
	
	@Test
	public void subtractTest() throws IOException {
		Scanner s = new Scanner("test/subtract.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void switchTest() throws IOException {
		Scanner s = new Scanner("test/switch.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void typesTest() throws IOException {
		Scanner s = new Scanner("test/types.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}

	@Test
	public void whileTest() throws IOException {
		Scanner s = new Scanner("test/while.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertTrue(p.parse());
	}
	
	@Test
	public void bad_addTest() throws IOException {
		Scanner s = new Scanner("test/bad_add.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertFalse(p.parse());
	}
	
	@Test
	public void bad_baseTest() throws IOException {
		Scanner s = new Scanner("test/bad_base.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertFalse(p.parse());
	}
	
	@Test
	public void bad_base_for_parenTest() throws IOException {
		Scanner s = new Scanner("test/bad_base_for_paren.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertFalse(p.parse());
	}
	
//	@Test
//	public void bad_base_parenTest() throws IOException {
//		Scanner s = new Scanner("test/bad_base_paren.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertFalse(p.parse());
//	}
	
	@Test
	public void bad_for_semi1Test() throws IOException {
		Scanner s = new Scanner("test/bad_for_semi1.c");
		s.scan();
		Parser p = new Parser(grammar, s);
		
		assertFalse(p.parse());
	}
	
//	@Test
//	public void base_semiTest() throws IOException {
//		Scanner s = new Scanner("test/base_semi.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}
	
//	@Test
//	public void base_semi2Test() throws IOException {
//		Scanner s = new Scanner("test/base_semi2.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}
	
//	@Test
//	public void for_semi2Test() throws IOException {
//		Scanner s = new Scanner("test/for_semi2.c");
//		s.scan();
//		Parser p = new Parser(grammar, s);
//		
//		assertTrue(p.parse());
//	}
	
}
