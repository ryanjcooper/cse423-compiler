package edu.nmt.optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;

public class IR {
	private Integer instrCount = 1;
	private List<Instruction> instructionList;
	private Map<String, List<Instruction>> functionIRs;
	private List<String> ignoredLabels = new ArrayList<String>(Arrays.asList(
			"compoundStmt"
		));
	
	public IR(Node root) {
		this.instructionList = new ArrayList<Instruction>();
		this.functionIRs = new HashMap<String, List<Instruction>>();
		this.buildFunctionIRs(root);
	}
	
	private Instruction buildInstructionList(Node node) {
		String label = node.getToken().getTokenLabel();
		Instruction add = null;
		
		if (label.contentEquals("compoundStmt")) {
			for (Node c : node.getChildren()) {
				this.buildInstructionList(c);
			}
		} else {
			List<Instruction> operandList = new ArrayList<Instruction>();
			if (!node.getChildren().isEmpty()) {
				for (Node c : node.getChildren()) {
					operandList.add(this.buildInstructionList(c));
				}
			}
			
			if (label.contentEquals("returnStmt")) {
				add = new ReturnInstruction(node, operandList, this.instrCount);
			} else if (label.contentEquals("call")) {
				add = new CallInstruction(node, operandList);
			} else if (label.contentEquals("ifStmt") || label.contentEquals("goto")) {
				add = new JumpInstruction(node, operandList);
			} else {
				add = new Instruction(node, operandList, this.instrCount);
			}
			
			this.instrCount++;
			this.instructionList.add(add);
		}

		return add;
		
	}
	
	public void buildFunctionIRs(Node root) {
		for (Node c : root.getChildren()) {
			if (c.getToken().getTokenLabel().contentEquals("funcDefinition")) {
				this.buildInstructionList(c.getChildren().get(0));
				this.functionIRs.put(c.getName(), new ArrayList<Instruction>(this.instructionList));
			}
			this.instructionList.clear();
		}
	}
	
	public static void printMain(Map<String, List<Instruction>> functionMap) {
		List<Instruction> instrList = functionMap.get("main");
		for (Instruction i : instrList) {
			System.out.println(i.getLineNumber() + ": " + i);
		}
	}

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
		Node root = a.getRoot();
		root.recursiveSetDepth();
		IR test = new IR(root);
		IR.printMain(test.getFunctionIRs());
		
	}

	public List<Instruction> getInstructionList() {
		return instructionList;
	}

	public void setInstructionList(List<Instruction> instructionList) {
		this.instructionList = instructionList;
	}

	public Map<String, List<Instruction>> getFunctionIRs() {
		return functionIRs;
	}

	public void setFunctionIRs(Map<String, List<Instruction>> functionIRs) {
		this.functionIRs = functionIRs;
	}

}
