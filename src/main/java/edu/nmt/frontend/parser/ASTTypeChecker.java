/**
 * 
 */
package edu.nmt.frontend.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.scanner.Scanner;

/**
 * @author Terence
 *
 */
public class ASTTypeChecker {
	
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner("test/test.c");
		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		if (p.parse()) {
			;
//			System.out.println(Node.printTree(p.getParseTree(), " ", false));	
		}
		
		ASTParser a = new ASTParser(p);
		if (a.parse()) {
			a.printAST();	
		}
		
		a.printSymbolTable();
		
		System.out.println(a.isTypedCorrectly());
	}

}
