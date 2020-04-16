//package edu.nmt.optimizer;
//
//import static org.junit.Assert.*;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//
//import org.junit.Test;
//
//import edu.nmt.frontend.Grammar;
//import edu.nmt.frontend.parser.ASTParser;
//import edu.nmt.frontend.parser.Parser;
//import edu.nmt.frontend.scanner.Scanner;
//import edu.nmt.optimizer.IR;
//
//public class IRIOTest {
//	
//	@Test
//	public void testConditions() throws IOException {
//		Scanner scanner = new Scanner("test/conditions.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//
//	@Test
//	public void testFor() throws IOException {
//		Scanner scanner = new Scanner("test/for.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//	
//	/*@Test
//	public void testFunction() throws IOException {
//		Scanner scanner = new Scanner("test/function.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	*/
//	
//	/*@Test
//	public void testGoto() throws IOException {
//		Scanner scanner = new Scanner("test/goto.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	*/
//	
//	@Test
//	public void testTypes() throws IOException {
//		Scanner scanner = new Scanner("test/types.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//	
//	@Test
//	public void testWhile() throws IOException {
//		Scanner scanner = new Scanner("test/while.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//
//	@Test
//	public void testBase() throws IOException {
//		Scanner scanner = new Scanner("test/base.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}
//
//	@Test
//	public void testAdd() throws IOException {
//		Scanner scanner = new Scanner("test/add.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}
//	
//	@Test
//	public void testDivide() throws IOException {
//		Scanner scanner = new Scanner("test/divide.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//	
//	@Test
//	public void testSubtract() throws IOException {
//		Scanner scanner = new Scanner("test/subtract.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//	
//	@Test
//	public void testMultiply() throws IOException {
//		Scanner scanner = new Scanner("test/multiply.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}		
//	
//	@Test
//	public void testMin() throws IOException {
//		Scanner scanner = new Scanner("test/min.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}		
//	
//	@Test
//	public void testBinary() throws IOException {
//		Scanner scanner = new Scanner("test/binary.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//	
//	@Test
//	public void testBoolean() throws IOException {
//		Scanner scanner = new Scanner("test/boolean.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//	
//	@Test
//	public void testBreak() throws IOException {
//		Scanner scanner = new Scanner("test/break.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}	
//	
//	@Test
//	public void testAssignBin() throws IOException {
//		Scanner scanner = new Scanner("test/assignment_bin.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}		
//	
//	@Test
//	public void testAssignArith() throws IOException {
//		Scanner scanner = new Scanner("test/assignment_arith.c");
//		scanner.scan();
//		
//		Grammar g = new Grammar("config/grammar.cfg");
//		g.loadGrammar();
//		
//		Parser p = new Parser(g, scanner, false);
//		p.parse();
//		
//		ASTParser a = new ASTParser(p);
//		a.parse();
//		
//		IR test = new IR(a);
//		test.outputToFile();
//		
//		IR testCopy = new IR();
//		testCopy.initFromFile(test.getFilename());
//		
//		assertTrue(test.equals(testCopy));
//	}		
//}
