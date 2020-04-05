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
 * @todo	implement loops (requires Jump), functions (requires Call), structs, switch statements, and goto
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
	
	public Integer getInstrCount() {
		return instrCount;
	}

	public void setInstrCount(Integer instrCount) {
		this.instrCount = instrCount;
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
			} else if(op.contentEquals("%")) {
				tokenString = "modExpression";
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
		numConst.setType("int");
		addExpression.addChild(numConst);
		addExpression.addChild(incExpr.getChildren().get(0));
		assignStmt.addChild(addExpression);
		assignStmt.addChild(idDuplicate);
		assignStmt.setParent(incExpr.getParent().getParent());
		
		return assignStmt;
	}
	
	private List<Instruction> buildInstruction(Node node) {
		List<Instruction> returnInstr = new ArrayList<Instruction>();
		List<Instruction> operandList = new ArrayList<Instruction>();
		String label = node.getToken().getTokenLabel();
		Instruction add = null;
		
		if (label.contentEquals("ifStmt")) {
			return this.buildConditional(node, null);
		}
		
		if (!node.getChildren().isEmpty() && !label.contentEquals("ifStmt")) {
			if (label.contains("IncExpr")) {
				// converts the AST representation of unary incrementation into its full formatting (e.g. a++ becomes a = a + 1)
				// then replaces the increment expression node with the new representation (which is an assignStmt)
				Node replace = IR.convertIncExpr(node);
				int nodeIndex = node.getParent().getParent().getChildren().indexOf(node.getParent());
				node.getParent().getParent().getChildren().set(nodeIndex, replace);
				node = replace;
				label = "assignStmt";
			}
			
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
				// index 1 contains the left-hand side of the assignment, so there is no need to recursively create instructions
				// this is not true for index 0, which is the right-hand side
				// the returned value will be a linearized instruction list of all the sub nodes of this statement in the AST
				returnInstr.addAll(this.buildInstruction(replace.getChildren().get(0)));
				
				// final member of tmp list is the root node of this node's subtree
				operandList.add(returnInstr.get(returnInstr.size() - 1));
			} else {
				for (Node c : node.getChildren()) {
					returnInstr.addAll(this.buildInstruction(c));
					operandList.add(returnInstr.get(returnInstr.size() - 1));
				}
			}
		}
		
		if (label.contentEquals("returnStmt")) {
			add = new ReturnInstruction(node, operandList, this.instrCount);
		} else if (label.contentEquals("call")) {
			add = new CallInstruction(node, operandList, this.instrCount);
		} else if(label.contentEquals("goto")) {
			add = new JumpInstruction(node, operandList, this.instrCount);
		} else {
			add = new Instruction(null, node, operandList, this.instrCount);
		}
		
		this.instrCount++;
		if (add != null) {
			returnInstr.add(add);
		}
		return returnInstr;
	}
	
	/* notes for implementation:
	 * first step is to convert the boolean expression into an instruction (which will later be added as an operand)
	 * next step is to create the destination if false as an instruction (this will also be added as an operand)
	 * afterwards, the body (destination if true) is added as an instruction (this will not be used as an operand)
	 * then, another jump must be added at the end of the body that leads to the instruction following the conditional block
	 * potential solution: create a dummy destination that all conditionals lead to, and then build rest of list after that
	 */
	private List<Instruction> buildConditional(Node ifStmt, Instruction finalDestination) {
		if (finalDestination == null) {
			finalDestination = new Instruction(null, new Node(new Token("endOfFullConditionalBlock", "label")), new ArrayList<Instruction>(), this.instrCount);
		}
		Instruction endOfBlock = new Instruction(null, new Node(new Token("endOfConditionalBlock", "label")), new ArrayList<Instruction>(), this.instrCount);
		List<Instruction> returnInstr = new ArrayList<Instruction>();
		List<Instruction> operandList1 = new ArrayList<Instruction>(); // operand list for conditional jump (if statement)
		List<Instruction> operandList2 = new ArrayList<Instruction>(); // operand list for unconditional jump (end of block)
		operandList2.add(finalDestination);
		Node condition = null;
		Node body = null;
		Node elseStmt = null;
		if (ifStmt.getChildren().size() == 1) {
			body = ifStmt.getChildren().get(0);
		} else {
			condition = ifStmt.getChildren().get(0);
			body = ifStmt.getChildren().get(1);
		}
		while (!body.getChildren().isEmpty() && ignoredLabels.contains(body.getChildren().get(0).getToken().getTokenLabel())) {
			body = body.getChildren().get(0);
		}
		if (ifStmt.getChildren().size() == 3) {
			elseStmt = ifStmt.getChildren().get(2);
		}
		
		// create boolean expression and add to operandList of JumpInstruction to be constructed
		if (condition != null) {
			returnInstr.addAll(this.buildInstruction(condition.getChildren().get(0)));
			operandList1.add(returnInstr.get(returnInstr.size() - 1));
			// add end of conditional block to operandList of JumpInstruction to be constructed
			operandList1.add(endOfBlock);
			returnInstr.add(new JumpInstruction(ifStmt, operandList1, this.instrCount));
			this.instrCount++;
		}
		// convert body nodes into instructions
		if (!body.getChildren().isEmpty()) {
			for (Node c : body.getChildren()) {
				returnInstr.addAll(this.buildInstruction(c));
			}
		}
		// unconditional jump to end of full conditional block
		if (condition != null) {
			returnInstr.add(new JumpInstruction(null, operandList2, this.instrCount));
			this.instrCount++;
		}
		
		endOfBlock.setLineNumber(this.instrCount);
		this.instrCount++;
		returnInstr.add(endOfBlock);
		
		if (elseStmt != null) {
			returnInstr.addAll(this.buildConditional(elseStmt, finalDestination));
		} else {
			
		}
		
		if (!ifStmt.getParent().getToken().getTokenLabel().contentEquals("ifStmt")) {
			finalDestination.setLineNumber(this.instrCount);
			returnInstr.add(finalDestination);
			this.instrCount++;
		}
		
		return returnInstr;
	}
	
	
	/**
	 * @todo	check if an assignStmt is nested under an assignStmt, and change how the Instruction nodes are constructed accordingly
	 * @param 	node
	 * @return	
	 */
	private void buildInstructionList(Node node) {
		String label = node.getToken().getTokenLabel();
		
		if (ignoredLabels.contains(label)) {
			// if the label is ignored, then continue to recursively build instruction list for children without constructing a new object
			for (Node c : node.getChildren()) {
				this.buildInstructionList(c);
			}
		} else {
			this.instructionList.addAll(this.buildInstruction(node));
		}
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
		Scanner scanner = new Scanner("test/functions.c");
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
