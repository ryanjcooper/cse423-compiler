package edu.nmt.optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.nmt.frontend.Grammar;
import edu.nmt.frontend.Node;
import edu.nmt.frontend.Token;
import edu.nmt.frontend.parser.ASTParser;
import edu.nmt.frontend.parser.Parser;
import edu.nmt.frontend.scanner.Scanner;

/**
 * 
 * @author	mattadik123
 * @todo	convert combo assignment operators (e.g. +=, ++)
 *
 */
public class IR {
	private Integer instrCount = 1;
	private List<Instruction> instructionList;
	private Map<String, List<Instruction>> functionIRs;
	private List<String> ignoredLabels = new ArrayList<String>(Arrays.asList(
			"compoundStmt",
			"statementList",
			"exprStmt"
		));
	private List<String> opAssign = new ArrayList<String>(Arrays.asList(
			"+=",
			"-=",
			"/=",
			"*=",
			"%=",
			"|=",
			"&=",
			">>=",
			"<<=",
			"^="
		));
	
	public IR(Node root) {
		this.instructionList = new ArrayList<Instruction>();
		this.functionIRs = new HashMap<String, List<Instruction>>();
		this.buildFunctionIRs(root);
	}
	
	private static Node convertAssignStmt(Node assignStmt) {
		String assignOp = assignStmt.getOp();
		String op = assignOp.replace("=", "");
		Node convert = null;
		Node newChild = null;
		if (op.isEmpty()) {
			return assignStmt;
		} else {
			Token tok = new Token(null, "assignStmt");
			convert = new Node(tok);
			convert.setOp("=");
			if (op.contentEquals("+") || op.contentEquals("-")) {
				Token tok2 = new Token(null, "addExpression");
				newChild = new Node(tok2);
			} else if (op.contentEquals("*") || op.contentEquals("/")) {
				Token tok2 = new Token(null, "mulExpression");
				newChild = new Node(tok2);
			} else {
				Token tok2 = new Token(null, "bitExpression");
				newChild = new Node(tok2);
			}

			newChild.setOp(op);
			newChild.setChildren(assignStmt.getChildren());
			List<Node> newChildren = new ArrayList<Node>();
			newChildren.add(newChild);
			newChildren.add(new Node(assignStmt.getChildren().get(1)));
			convert.setChildren(newChildren);
		}
		
		return convert;
	}
	
	private Instruction buildInstructionList(Node node) {
		String label = node.getToken().getTokenLabel();
		Instruction add = null;
		
		if (ignoredLabels.contains(label)) {
			for (Node c : node.getChildren()) {
				this.buildInstructionList(c);
			}
		} else {
			List<Instruction> operandList = new ArrayList<Instruction>();
			if (!node.getChildren().isEmpty()) {
				if (!label.contentEquals("assignStmt")) {
					for (Node c : node.getChildren()) {
						operandList.add(this.buildInstructionList(c));
					}
				} else {
					Node replace = IR.convertAssignStmt(node);
					if (node != replace) {
						int nodeIndex = node.getParent().getChildren().indexOf(node);
						node.getParent().getChildren().set(nodeIndex, replace);
					}

					operandList.add(this.buildInstructionList(replace.getChildren().get(0)));
					
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
			a.printAST();	
		}
		
		a.printSymbolTable();
		Node root = a.getRoot();
		root.recursiveSetDepth();
		Node testing = root.getChildren().get(0).getChildren().get(0).getChildren().get(0);
		IR test = new IR(root);
		List<Instruction> testing2 = test.getFunctionIRs().get("main");
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
