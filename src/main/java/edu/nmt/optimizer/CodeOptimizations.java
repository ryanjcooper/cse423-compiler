package edu.nmt.optimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
			if(splitres.length != 2) {
				
				eval = new ExpressionEvaluator(splitres[0]);
				
				if(eval.GetValue() != null) {
					try {
			    		i.setOperation("identifier");
			    		i.op1Name  = Integer.toString(eval.GetValue().intValue());
			    		
			    		if(Integer.parseInt(splitres[0]) != eval.GetValue().intValue()) {
			    			status = true;
			    		}
			    	} catch (NullPointerException e) {
			    		i.op1Name  = Integer.toString(eval.GetValue().intValue());
			    		if(Integer.parseInt(splitres[0]) != eval.GetValue().intValue()) {
			    			status = true;
			    		}
					} catch (NumberFormatException e) {
						status = false;
					}
				}
				continue;
			}
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
	
	/**
	 * Applies constant propagation to the IR
	 * @return true if something was changed
	 */
	public Boolean constProp() {
		String splitres[];
		List<Instruction> instrList = target.getFunctionIRs().get("main");
		Map<String, Integer> varMap = new HashMap<String, Integer>();
		Map<String, Integer> copy = null;
		List<String> removalKeys = new ArrayList<String>();
		Stack<Map<String, Integer>> scoping = new Stack<Map<String, Integer>>();
		String json = null;
		Boolean status = false;
		
		for (Instruction i : instrList) {
			System.out.println("List is currently: " + varMap);
			// Add or update values in list
			splitres = i.toString().split("=");
			if(splitres[0].contains("jump")) {
				status = false;
				scoping.push(varMap);
				// Create Deep Clone
				copy = new HashMap<String, Integer>();
				for(Map.Entry<String, Integer> entry : varMap.entrySet()) {
					copy.put(entry.getKey(), entry.getValue());
				}
				varMap = copy;
				System.out.println("Stacked");
				continue;
			} else if(splitres.length > 1 && splitres[1].contains("end")) {
				// Return scoped list
				System.out.println("Unstacked");
				copy = varMap;
				varMap = scoping.pop();
				// Mark changed values
//				System.out.println("************** Entry checking *********************88");
				removalKeys = new ArrayList<String>();
//				System.out.println("List is: " + copy);
				for(Map.Entry<String, Integer> entry : copy.entrySet()) {
					try {
						if (entry.getValue().equals(varMap.get(entry.getKey())) == false) {
							removalKeys.add(entry.getKey());
//							System.out.println("************** Entry marked *********************88");
						}
					} catch(NullPointerException e) {
						removalKeys.add(entry.getKey());
					}
					
				}
				// Remove values
				for (int j = 0; j < removalKeys.size(); j++) {
					varMap.replace(removalKeys.get(j), null);
				}
//				System.out.println("List is: " + varMap);
			}
			
			if(splitres.length != 2) {
				for (String key : varMap.keySet()) {
				    if (splitres[0].contains(key)) {
				    	// Case for key words
				    	try {
				    		splitres[0] = splitres[0].replaceAll(key, varMap.get(key).toString());
				    		splitres[0] = splitres[0].replaceAll("return", "");
				    		splitres[0] = splitres[0].replaceAll("\\s+", "");
				    		i.op1Name  = splitres[0];
				    		status = true;
				    	} catch (NullPointerException e) {
//				    		System.out.println(i.toString());
				    		splitres[0] = splitres[0].replaceAll(key, varMap.get(key).toString());
				    		splitres[0] = splitres[0].replaceAll("return", "");
				    		splitres[0] = splitres[0].replaceAll("\\s+", "");
				    		i.op1Name  = splitres[0];
							status = true;
						}
				    }
				}
				continue;
			}
			splitres[0] = splitres[0].replaceAll("\\s+", "");
			splitres[1] = splitres[1].replaceAll("\\s+", "");
			
			//Try to propagate the rhs
			for (String key : varMap.keySet()) {
			    if (splitres[1].contains(key)) {
			    	// Modify for 2 possible types
//			    	System.out.println(i.toString());
			    	try {
			    		key = key + "(?!.+[0-9])";
			    		System.out.println("THIS IS THE STRING FOR REPS: " + key);
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
		List<Instruction> deadLines = new ArrayList<Instruction>();
		int count = 0;

		// Find Dead lines
		for (Instruction i : instrList) {
			// Add or update values in list
			splitres = i.toString().split("=");
			if(splitres.length != 2) {
				continue;
			}
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
	
	/**
	 * Wraps all Level 1 optimizations together
	 * @param target
	 * @return Optimized IR
	 */
	public static void l1Optimize(IR target) {
		Boolean status;
		CodeOptimizations o1 = new CodeOptimizations(target);

		status = true;
		while(status) {
			status = false;
			IR.printMain(target.getFunctionIRs());
			status = o1.constProp();
			IR.printMain(target.getFunctionIRs());
			status |= o1.constFold();
		}
		//o1.clean();
		
		return;
	}
	
	public static void main(String[] args) throws Exception {
		Scanner scanner = new Scanner("test/add.c");

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
		
		a.printSymbolTable();
		Node root = a.getRoot();
		root.recursiveSetDepth();
//		Node mainAST = root.getChildren().get(0).getChildren().get(0).getChildren().get(0);
		IR test = new IR(a);
		List<Instruction> mainList = test.getFunctionIRs().get("main");
//		System.out.println(mainList.get(0));
		IR.printMain(test.getFunctionIRs());
		
		//test.printIR();
		
		test.outputToFile();
		IR tmp = new IR();
		tmp.initFromFile(test.getFilename());
		System.out.println(test.equals(tmp));
		
		System.out.println("Pre-Optimization");
		IR.printMain(test.getFunctionIRs());
		CodeOptimizations.l1Optimize(test); //  Example for calling L1 Opt
		System.out.println("\nPost-Optimization");
		IR.printMain(test.getFunctionIRs());
		
		ExpressionEvaluator eval;
		eval = new ExpressionEvaluator("5 * 70");
		System.out.println(eval.GetValue());
	}

}
