package edu.nmt.optimizer;

import java.util.List;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;

public class CodeOptimizations {
	
	public IR target;
	
	/*
	 * Base constructor using formed IR
	 */
	public CodeOptimizations(IR a) {
		target = a;
	}
	
	/*
	 * Applies constant folding in place to the IR
	 */
	public void constFold() {
		List<Instruction> instrList = target.getFunctionIRs().get("main");
		System.out.println("Folding");
		for (Instruction i : instrList) {
			System.out.println(i.getLineNumber() + ": " + i + " type: " + i.getType());
			if(!i.operation.isEmpty())
			System.out.println(i.operand1 + i.operation + i.operand2);
		}
		
		return;
	}
	
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner("test/assignment_arith.c");
		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		if (p.parse()) {
//			p.printParseTree();
		}
		
		ASTParser a = new ASTParser(p);
		if (a.parse()) {
//			a.printAST();	
		}
		
//		a.printSymbolTable();
		Node root = a.getRoot();
		root.recursiveSetDepth();
		Node mainAST = root.getChildren().get(0).getChildren().get(0).getChildren().get(0);
		IR test = new IR(a);
		List<Instruction> mainList = test.getFunctionIRs().get("main");
		IR.printMain(test.getFunctionIRs());
		
		CodeOptimizations o1 = new CodeOptimizations(test);
		o1.constFold();
	}

}
