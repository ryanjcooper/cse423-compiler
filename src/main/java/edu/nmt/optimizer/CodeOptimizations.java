package edu.nmt.optimizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//			if(!i.operation.isEmpty()) {
//				System.out.println(i.operand1 + i.operation + i.operand2);
//			}
		}
		
		return;
	}
	
	public Boolean constProp() {
		String splitres[];
		List<Instruction> instrList = target.getFunctionIRs().get("main");
		Map<String, Integer> varMap = new HashMap<String, Integer>();
		Instruction tmp;
		Boolean status = false;
		System.out.println("propagating");
		
		for (Instruction i : instrList) {
			// Add or update values in list
			splitres = i.toString().split("=");
			splitres[0] = splitres[0].replaceAll("\\s+", "");
			splitres[1] = splitres[1].replaceAll("\\s+", "");
			
			//Try to propagate the rhs
			if(varMap.get(splitres[1]) != null) {
				try {
					i.setOperation("identifier");
					i.op1Name  = i.operand1.op1Name;
					status = true;
				} catch (NullPointerException e) {
					i.setOp1Name(varMap.get(splitres[1]).toString());
					status = true;
				}
			}
			
			// Add to or update map
			if(varMap.containsKey(splitres[0])) {
				try {
				varMap.replace(splitres[0], Integer.parseInt(splitres[1]));
				} catch (NumberFormatException e) {
					varMap.replace(splitres[0], null);
				}
			} else {
				try {
					varMap.put(splitres[0], Integer.parseInt(splitres[1]));
					} catch (NumberFormatException e) {
						varMap.replace(splitres[0], null);
					}
			}
			
			// Test statement to print map
//			for (Map.Entry<String, Integer> entry : varMap.entrySet()) {
//			    System.out.println(entry.getKey() + ":" + entry.getValue().toString());
//			}
		}
		
		return status;
	}
	
	public static void main(String[] args) throws Exception {
		Boolean status;
		Scanner scanner = new Scanner("test/foldproptest.c");
		scanner.scan();
		Grammar g = new Grammar("config/grammar.cfg");
		g.loadGrammar();
		Parser p = new Parser(g, scanner, false);
		if (p.parse()) {
//			p.printParseTree();
		}
		
		ASTParser a = new ASTParser(p);
		if (a.parse()) {
			a.printAST();	
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
		
		status = true;
		while(status) {
			status = o1.constProp();
			IR.printMain(o1.target.getFunctionIRs());
		}
		IR.printMain(o1.target.getFunctionIRs());
	}

}
