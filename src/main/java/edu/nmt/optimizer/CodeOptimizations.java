package edu.nmt.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
	public Boolean constFold() {
		String splitres[];
		List<Instruction> instrList = target.getFunctionIRs().get("main");
		ExpressionEvaluator eval;
		Boolean status = false;
		
		for (Instruction i : instrList) {
			splitres = i.toString().split("=");
			splitres[0] = splitres[0].replaceAll("\\s+", "");
			splitres[1] = splitres[1].replaceAll("\\s+", "");
			
			eval = new ExpressionEvaluator(splitres[1]);
			
			if(eval.GetValue() != null) {
				try {
		    		i.setOperation("identifier");
		    		i.op1Name  = Integer.toString(eval.GetValue().intValue());
		    		
		    		if(Integer.parseInt(splitres[1]) != eval.GetValue().intValue()) {
		    			status = true;
		    		}
		    	} catch (NullPointerException e) {
		    		i.op1Name  = Integer.toString(eval.GetValue().intValue());
		    		if(Integer.parseInt(splitres[1]) != eval.GetValue().intValue()) {
		    			status = true;
		    		}
				} catch (NumberFormatException e) {
					status = false;
				}
			}
			
			
			
		}
		
		return status;
	}
	
	public Boolean constProp() {
		String splitres[];
		List<Instruction> instrList = target.getFunctionIRs().get("main");
		Map<String, Integer> varMap = new HashMap<String, Integer>();
		Instruction tmp;
		Boolean status = false;
		
		for (Instruction i : instrList) {
			// Add or update values in list
			splitres = i.toString().split("=");
			splitres[0] = splitres[0].replaceAll("\\s+", "");
			splitres[1] = splitres[1].replaceAll("\\s+", "");
			
			//Try to propagate the rhs
			for (String key : varMap.keySet()) {
			    if (splitres[1].contains(key)) {
			    	// Modify for 2 possible types
			    	try {
			    		splitres[1] = splitres[1].replaceAll(key, varMap.get(key).toString());
			    		i.setOperation("identifier");
			    		i.op1Name  = splitres[1];
			    		status = true;
			    	} catch (NullPointerException e) {
						i.setOp1Name(varMap.get(splitres[1]).toString());
						status = true;
					}
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
	
	/**
	 * Remove unnecessary lines obsoleted by fold and prop
	 */
	public void clean() {
		String splitres[];
		List<Instruction> instrList = target.getFunctionIRs().get("main");
		Map<String, Integer> varMap = new HashMap<String, Integer>();
		List<Instruction> deadLines = new ArrayList<Instruction>();
		final Pattern isTmp = Pattern.compile("^_[0-9]*");
		int count = 0;

		// Find Dead lines
		for (Instruction i : instrList) {
			// Add or update values in list
			splitres = i.toString().split("=");
			splitres[0] = splitres[0].replaceAll("\\s+", "");
			splitres[1] = splitres[1].replaceAll("\\s+", "");
			
			// Check if RHS is a constant
			try {
				// If RHS is a const and left a temp variable, remove
				Integer.parseInt(splitres[1]);
				if(splitres[0].charAt(0) == '_') {
					deadLines.add(i);
				}
				
			} catch (NumberFormatException e) {
			}
		}
		
		// Remove Dead Lines
		instrList.removeAll(deadLines);
		
		count = 1;
		for (Instruction i : instrList) {
			i.lineNumber = count;
			count++;
		}
		this.target.setInstrCount(count);
		
		return;
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

		
		status = true;
		while(status) {
			status = false;
			status = o1.constProp();
			status |= o1.constFold();
		}
		
		o1.clean();
		
		System.out.println("Post-Optimize");
		IR.printMain(o1.target.getFunctionIRs());
		
		
	}

}
