package edu.nmt.optimizer;

import java.io.File;
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
 * @todo	implement conditions (requires Jump), loops (requires Jump), functions (requires Call), structs, switch statements
 *
 */
public class IR {
	ASTParser a;
	public static Map<String, Instruction> variableMap = new HashMap<String, Instruction>();
	private Integer instrCount = 1;
	private List<Instruction> instructionList;
	private Map<String, List<Instruction>> functionIRs;
	private List<String> ignoredLabels = new ArrayList<String>(Arrays.asList(
			"compoundStmt",
			"statementList",
			"exprStmt"
		));
	
	public IR() {
		this.instructionList = new ArrayList<Instruction>();
	}
	
	public IR(ASTParser a) {
		this(a.getRoot());
		this.a = a;
	}
	
	public IR(Node root) {
		this.instructionList = new ArrayList<Instruction>();
		this.functionIRs = new HashMap<String, List<Instruction>>();
		this.buildFunctionIRs(root);
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
	
	private static Node convertAssignStmt(Node assignStmt) {
		String assignOp = assignStmt.getOp();
		String op = assignOp.replace("=", "");
		Node convert = null;
		Node newChild = null;
		if (op.isEmpty()) {
			return assignStmt;
		} else {
			convert = new Node(new Token(null, "assignStmt"));
			convert.setOp("=");
			String tokenString = null;
			if (op.contentEquals("+") || op.contentEquals("-")) {
				tokenString = "addExpression";
			} else if (op.contentEquals("*") || op.contentEquals("/")) {
				tokenString = "mulExpression";
			} else {
				tokenString = "bitExpression";
			}

			newChild = new Node(new Token(null, tokenString));
			newChild.setOp(op);
			newChild.setChildren(assignStmt.getChildren());
			List<Node> newChildren = new ArrayList<Node>();
			newChildren.add(newChild);
			newChildren.add(new Node(assignStmt.getChildren().get(1)));
			convert.setChildren(newChildren);
		}
		
		return convert;
	}
	
	private static Node convertIncExpr(Node incExpr) {
		Node assignStmt = new Node(new Token(null, "assignStmt"));
		Node addExpression = new Node(new Token(null, "addExpression"));
		Node numConst = new Node(new Token("1"));
		Node idDuplicate = new Node(incExpr.getChildren().get(0));
		
		assignStmt.setOp("=");
		addExpression.setOp(Character.toString(incExpr.getOp().charAt(0)));
		addExpression.addChild(numConst);
		addExpression.addChild(incExpr.getChildren().get(0));
		assignStmt.addChild(addExpression);
		assignStmt.addChild(idDuplicate);
		
		return assignStmt;
	}
	
	private Instruction buildInstruction(Node node) {
		List<Instruction> operandList = new ArrayList<Instruction>();
		String label = node.getToken().getTokenLabel();
		Instruction add = null;
		
		if (!node.getChildren().isEmpty()) {
			if (label.contentEquals("assignStmt")) {
				// converts the AST representation of combination assignment statements (e.g. x += y) into its full formatting (e.g. x = x + y)
				Node replace = IR.convertAssignStmt(node);
				// node == replace when the assignment statement is not a combination assignment
				// when node != replace, node is replaced in its parent's child list by replace
				if (node != replace) {
					int nodeIndex = node.getParent().getChildren().indexOf(node);
					node.getParent().getChildren().set(nodeIndex, replace);
					node = replace;
				}

				operandList.add(this.buildInstruction(replace.getChildren().get(0)));
			} else if (label.contains("incExpr")) {
				// converts the AST representation of unary incrementation into its full formatting (e.g. a++ becomes a = a + 1)
				// then replaces the increment expression node with the new representation
				Node replace = IR.convertIncExpr(node);
				int nodeIndex = node.getParent().getParent().getChildren().indexOf(node.getParent());
				node.getParent().getParent().getChildren().set(nodeIndex, replace);
				node = replace;
				
				operandList.add(this.buildInstruction(replace.getChildren().get(0)));
			} else {
				for (Node c : node.getChildren()) {
					operandList.add(this.buildInstruction(c));
				}
			}
		}
		
		if (label.contentEquals("returnStmt")) {
			add = new ReturnInstruction(node, operandList, this.instrCount);
		} else if (label.contentEquals("call")) {
			add = new CallInstruction(node, operandList, this.instrCount);
		} else if (label.contentEquals("ifStmt") || label.contentEquals("goto")) {
			add = new JumpInstruction(node, operandList, this.instrCount);
		} else {
			add = new Instruction(null, node, operandList, this.instrCount);
		}
		
		this.instrCount++;
		this.instructionList.add(add);
		return add;
	}
	
	
	/**
	 * @todo	check if an assignStmt is nested under an assignStmt, and change how the Instruction nodes are constructed accordingly
	 * @param 	node
	 * @return	
	 */
	private Instruction buildInstructionList(Node node) {
		String label = node.getToken().getTokenLabel();
		Instruction add = null;
		
		if (ignoredLabels.contains(label)) {
			// if the label is ignored, then continue to recursively build instruction list for children without constructing a new object
			for (Node c : node.getChildren()) {
				this.buildInstructionList(c);
			}
		} else {
			add = this.buildInstruction(node);
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
	
	public void fileToIR(String fileName) {
		java.util.Scanner irScanner = null;
		
		try {
			File irFile = new File(fileName);
			irScanner = new java.util.Scanner(irFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (irScanner.hasNext()) {
			String line = irScanner.next();
			this.instructionList.add(Instruction.strToInstr(line));
		}
		
		irScanner.close();
	}
	
	public static void printMain(Map<String, List<Instruction>> functionMap) {
		List<Instruction> instrList = functionMap.get("main");
		for (Instruction i : instrList) {
			System.out.println(i.getLineNumber() + ": " + i + " type: " + i.getType());
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
		Node mainAST = root.getChildren().get(0).getChildren().get(0).getChildren().get(0);
		IR test = new IR(a);
		List<Instruction> mainList = test.getFunctionIRs().get("main");
		System.out.println(mainList.get(0));
		IR.printMain(test.getFunctionIRs());
		
	}
}
