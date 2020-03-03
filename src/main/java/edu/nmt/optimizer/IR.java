package edu.nmt.optimizer;

import java.util.List;
import java.util.Map;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;

public class IR {
	
	private List<Instruction> instructionList;
	private Map<String, List<Instruction>> functionIRs;

	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner("test/base.c");
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
		a.getRoot().recursiveSetDepth();
		
		List<Instruction> instrList = null;
		
		for (int maxDepth = a.getRoot().getMaxDepth(); maxDepth > 0; maxDepth--) {
			
		}
	}

}
